package name.ulbricht.skat.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import name.ulbricht.skat.ai.Advice;
import name.ulbricht.skat.ai.ComputerMove;
import name.ulbricht.skat.ai.ComputerPlayer;
import name.ulbricht.skat.ai.HandEvaluator;
import name.ulbricht.skat.ai.SimplePlayer;
import name.ulbricht.skat.ai.Strength;
import name.ulbricht.skat.engine.CompletedTrick;
import name.ulbricht.skat.engine.Game;
import name.ulbricht.skat.engine.Phase;
import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.engine.Session;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.model.Suit;
import name.ulbricht.skat.rules.GameResult;
import name.ulbricht.skat.sound.Sound;
import name.ulbricht.skat.sound.SoundPlayer;

/**
 * Runs the session on screen: renders the current state of the game into the {@link TableScreen},
 * reacts to the player's clicks, and lets the computer players act one after another with a
 * short pause so the player can follow.
 */
final class GameController {

    private static final Duration BID_DELAY = Duration.millis(900);
    private static final Duration PLAY_DELAY = Duration.millis(1000);
    private static final Duration DECIDE_DELAY = Duration.millis(1400);
    /** Pause after the player's own card, so there is time to take it back. */
    private static final Duration TAKE_BACK_DELAY = Duration.millis(2500);
    /** How long a complete trick stays on the table before it is collected. */
    private static final Duration TRICK_DELAY = Duration.millis(2000);
    private static final int ALL_TRICKS_COLUMNS = 3;
    /** Card width in the window with all tricks. */
    private static final double SMALL_CARD = 46;
    /** Card width of the samples in the settings. */
    private static final double SAMPLE_CARD = 48;
    private static final int TOTAL_POINTS = 120;

    private final Texts texts = new Texts();
    private final Describe describe = new Describe(texts);
    private final TableScreen screen = new TableScreen();
    private final Session session;
    private final Settings settings;
    private final SoundPlayer sounds;
    /** Draws the seeds for the computer players, which are created anew when the strength changes. */
    private final Random seeds;
    private ComputerPlayer computer;
    /** Gives the human player tips with the rules of thumb of the "Normal" strength. */
    private final SimplePlayer advisor = new SimplePlayer();
    private final RulesView rules;
    /** The tip the player asked for; the next move clears it. */
    private Advice tip;
    /** Set once the sound for the end of the game has been played. */
    private boolean resultSounded;
    private final Map<PlayerId, String> bubbles = new EnumMap<>(PlayerId.class);
    private final List<Card> discardSelection = new ArrayList<>();
    private final PauseTransition computerTimer = new PauseTransition();
    /** Collects a complete trick after a short look at it. */
    private final PauseTransition collectTimer = new PauseTransition();
    private PlayerId lastSayer;
    /** The player whose card has just been played and should fly into the trick. */
    private PlayerId justPlayed;
    /** True while the trick slides to its winner; clicks are ignored meanwhile. */
    private boolean collecting;
    private boolean animated = true;
    private final Announcements announcements = new Announcements();
    private boolean showAnnouncements;
    /** A window-like panel in front of the table (menu, last trick, ...); the game waits meanwhile. */
    private Node overlay;
    private Pos overlayPosition = Pos.CENTER;

    GameController(long seed, Settings settings, SoundPlayer sounds) {
        session = new Session(seed, PlayerId.RIGHT);
        seeds = new Random(seed);
        this.settings = settings;
        this.sounds = sounds;
        computer = settings.strength().create(seed);
        rules = new RulesView(texts, closeButton());
        useDeck(settings.deck());
        computerTimer.setOnFinished(event -> computerStep());
        collectTimer.setOnFinished(event -> onCollect());
    }

    Parent node() {
        return screen.node();
    }

    /** Deals the first game. */
    void start() {
        newGame();
    }

    /** Switches the card animations on or off; off is used for screenshots. */
    void setAnimated(boolean animated) {
        this.animated = animated;
    }

    Game game() {
        return session.game();
    }

    Seat humanSeat() {
        return session.seatOf(PlayerId.HUMAN);
    }

    /** Tells whether a computer player has to act next. */
    boolean isComputerTurn() {
        Seat acting = game().actingSeat();
        return acting != null && session.playerAt(acting) != PlayerId.HUMAN;
    }

    // ----------------------------------------------------------------------------------------
    // Flow

    private void newGame() {
        if (session.isGameRunning()) {
            session.abandonGame();
        }
        overlay = null;
        tip = null;
        resultSounded = false;
        session.startGame();
        bubbles.clear();
        discardSelection.clear();
        lastSayer = null;
        announcements.reset();
        showAnnouncements = false;
        refresh();
    }

    /** Lets the computer player on turn act once. */
    void computerStep() {
        if (!isComputerTurn()) {
            return;
        }
        tip = null;
        Game game = game();
        Seat acting = game.actingSeat();
        if (game.phase() == Phase.BIDDING) {
            applyBid(acting, computer.acceptBid(new PlayerView(game, acting)));
        } else {
            if (game.phase() == Phase.PLAYING) {
                justPlayed = session.playerAt(acting);
                sound(Sound.CARD);
            }
            ComputerMove.perform(game, computer);
        }
        if (game.actingSeat() == humanSeat()) {
            sound(Sound.TURN);
        }
        refresh();
    }

    private void applyBid(Seat seat, boolean accept) {
        tip = null;
        Game game = game();
        PlayerId player = session.playerAt(seat);
        if (accept) {
            if (game.isAskingToSay()) {
                bubbles.put(player, String.valueOf(game.pendingBidValue()));
                lastSayer = player;
            } else {
                bubbles.put(player, texts.get("bid.bubble.yes"));
            }
            game.acceptBid();
        } else {
            bubbles.put(player, texts.get("bid.bubble.pass"));
            game.passBid();
        }
    }

    private void refresh() {
        computerTimer.stop();
        collectTimer.stop();
        Game game = game();
        if (game.phase() == Phase.FINISHED && !resultSounded) {
            resultSounded = true;
            sound(humanWon(game) ? Sound.WON : Sound.LOST);
        }
        renderTopBar(game);
        renderOpponents(game);
        renderCenter(game);
        renderHand(game);
        renderPanelAndHint(game);
        screen.setOverlay(overlay, overlayPosition);
        if (overlay == null && isComputerTurn()) {
            computerTimer.setDuration(delay(game));
            computerTimer.playFromStart();
        }
        if (overlay == null && game.phase() == Phase.TRICK_COMPLETE && !collecting) {
            // The complete trick stays a moment so it can be looked at, longer if the own card may still
            // be taken back; then it is collected without a click.
            collectTimer.setDuration(game.canTakeBack(humanSeat()) ? TAKE_BACK_DELAY : TRICK_DELAY);
            collectTimer.playFromStart();
        }
    }

    private Duration delay(Game game) {
        switch (game.phase()) {
            case BIDDING:
                return BID_DELAY;
            case PLAYING:
                // Right after the player's own card, wait longer so it can still be taken back.
                return game.canTakeBack(humanSeat()) ? TAKE_BACK_DELAY : PLAY_DELAY;
            default:
                return DECIDE_DELAY;
        }
    }

    // ----------------------------------------------------------------------------------------
    // Player actions

    void onBid(boolean accept) {
        applyBid(humanSeat(), accept);
        refresh();
    }

    void onTakeSkat() {
        tip = null;
        game().takeSkat();
        refresh();
    }

    void onPlayHand() {
        tip = null;
        game().playHand();
        refresh();
    }

    void onDiscard() {
        tip = null;
        game().discard(discardSelection.get(0), discardSelection.get(1));
        discardSelection.clear();
        refresh();
    }

    void onDeclare(Declaration declaration) {
        tip = null;
        game().declare(declaration);
        refresh();
    }

    void onCard(Card card) {
        tip = null;
        Game game = game();
        if (game.phase() == Phase.PLAYING && game.actingSeat() == humanSeat()) {
            justPlayed = PlayerId.HUMAN;
            sound(Sound.CARD);
            game.play(card);
        } else if (game.phase() == Phase.DISCARD && game.declarer() == humanSeat()) {
            if (!discardSelection.remove(card)) {
                if (discardSelection.size() == 2) {
                    discardSelection.remove(0);
                }
                discardSelection.add(card);
            }
        }
        refresh();
    }

    /** Collects the complete trick; called by the timer after a short look at the trick. */
    void onCollect() {
        Game game = game();
        if (collecting || game.phase() != Phase.TRICK_COMPLETE) {
            return;
        }
        tip = null;
        sound(Sound.COLLECT);
        if (!animated) {
            game.collectTrick();
            refresh();
            return;
        }
        // While the trick slides to its winner the table takes no clicks, so nothing can change the
        // game in between; the check on the game is a second safety net.
        collecting = true;
        screen.setInputBlocked(true);
        screen.animateCollect(spot(session.playerAt(game.currentTrickWinner())), () -> {
            collecting = false;
            screen.setInputBlocked(false);
            if (game == game() && game.phase() == Phase.TRICK_COMPLETE) {
                game.collectTrick();
            }
            refresh();
        });
    }

    void onTakeBack() {
        if (collecting) {
            return;
        }
        tip = null;
        game().takeBack(humanSeat());
        refresh();
    }

    /** Asks the computer for the best move and shows it with its reason; nothing is played yet. */
    void onTip() {
        Game game = game();
        if (game.actingSeat() != humanSeat()) {
            return;
        }
        PlayerView view = new PlayerView(game, humanSeat());
        switch (game.phase()) {
            case BIDDING:
                tip = advisor.adviseBid(view);
                break;
            case SKAT_DECISION:
                tip = advisor.adviseSkat(view);
                break;
            case DISCARD:
                tip = advisor.adviseDiscard(view);
                discardSelection.clear();
                discardSelection.addAll(tip.cards());
                break;
            case DECLARATION:
                tip = advisor.adviseDeclaration(view);
                // Set the announcements to the suggested game, so the marked button plays exactly it.
                announcements.reset();
                if (tip.declaration().isOuvert()) {
                    announcements.toggleOuvert();
                    showAnnouncements = true;
                }
                break;
            default:
                tip = advisor.advisePlay(view);
                break;
        }
        refresh();
    }

    /** Opens the rules at the chapter that explains the current situation. */
    void onWhy() {
        showOverlay(rules.node(topicFor(game())), Pos.CENTER);
    }

    void onRules() {
        showOverlay(rules.node(RuleTopic.OVERVIEW), Pos.CENTER);
    }

    void onSettings() {
        showOverlay(settingsPanel(), Pos.CENTER);
    }

    void onAllTricks() {
        showOverlay(allTricksPanel(), Pos.CENTER);
    }

    void onToggleSound() {
        settings.setSoundOn(!settings.isSoundOn());
        refresh();
    }

    private static RuleTopic topicFor(Game game) {
        switch (game.phase()) {
            case BIDDING:
            case PASSED_IN:
                return RuleTopic.BIDDING;
            case SKAT_DECISION:
            case DISCARD:
                return RuleTopic.SKAT;
            case DECLARATION:
                return RuleTopic.VALUE;
            case FINISHED:
                return RuleTopic.WINNING;
            default:
                return RuleTopic.FOLLOW;
        }
    }

    private void sound(Sound sound) {
        if (settings.isSoundOn()) {
            sounds.play(sound);
        }
    }

    void onNextGame() {
        newGame();
    }

    void onMoreAnnouncements() {
        showAnnouncements = true;
        refresh();
    }

    void onMenu() {
        showOverlay(menuPanel(), Pos.TOP_RIGHT);
    }

    void onLastTrick() {
        showOverlay(lastTrickPanel(), Pos.CENTER);
    }

    // ----------------------------------------------------------------------------------------
    // Rendering

    private void renderTopBar(Game game) {
        List<String> items = new ArrayList<>();
        String title;
        String points = null;
        switch (game.phase()) {
            case BIDDING:
                title = texts.get("top.bidding");
                items.add(texts.get("top.dealer", name(session.dealer())));
                items.add(texts.get("top.yourSeat", texts.seat(humanSeat())));
                break;
            case PASSED_IN:
                title = texts.get("top.passedIn");
                break;
            case SKAT_DECISION:
            case DISCARD:
            case DECLARATION:
                title = texts.get("top.declaring");
                items.add(teamLine(game));
                items.add(texts.get("top.bid", game.bid()));
                if (game.phase() == Phase.DECLARATION && game.declarer() == humanSeat() && !game.isHand()) {
                    points = texts.get("points.skat", Card.totalPoints(game.skat()));
                }
                break;
            default:
                // Ordered by importance: if the top bar is too narrow, the bid is left out first.
                title = describe.gameName(game.declaration());
                items.add(teamLine(game));
                if (game.phase() != Phase.FINISHED) {
                    items.add(texts.get("top.trick", Math.min(game.tricks().size() + 1, GameResult.TRICKS)));
                }
                items.add(texts.get("top.bid", game.bid()));
                points = pointsBadge(game);
                break;
        }
        Button sound = button(texts.get(settings.isSoundOn() ? "sound.on" : "sound.off"), event -> onToggleSound(),
                "top-button");
        Button menu = button(texts.get("menu.button"), event -> onMenu(), "top-button");
        screen.setTopBar(title, items, points, sound, menu);
    }

    /** Returns who plays with whom, e.g. "Du spielst allein" or "Du und Ludwig gegen Richard". */
    private String teamLine(Game game) {
        PlayerId declarer = session.playerAt(game.declarer());
        if (declarer == PlayerId.HUMAN) {
            return texts.get("team.youAlone");
        }
        return texts.get("team.withPartner", name(partnerOfHuman(declarer)), name(declarer));
    }

    private static PlayerId partnerOfHuman(PlayerId declarer) {
        return declarer == PlayerId.LEFT ? PlayerId.RIGHT : PlayerId.LEFT;
    }

    private String pointsBadge(Game game) {
        Seat human = humanSeat();
        Seat declarer = game.declarer();
        // The skat only counts once the player may know it: as declarer after taking it up, or at the end.
        boolean skatKnown = human == declarer && !game.isHand();
        int declarerPoints = game.phase() == Phase.FINISHED ? game.result().declarerPoints()
                : game.trickPoints(declarer) + (skatKnown ? Card.totalPoints(game.skat()) : 0);
        int opponentPoints = 0;
        for (Seat seat : Seat.values()) {
            if (seat != declarer) {
                opponentPoints += game.trickPoints(seat);
            }
        }
        if (game.phase() == Phase.FINISHED) {
            opponentPoints = TOTAL_POINTS - declarerPoints;
        }
        if (human == declarer) {
            return texts.get("points.declarer", declarerPoints, opponentPoints);
        }
        return texts.get("points.opponents", opponentPoints, name(session.playerAt(declarer)), declarerPoints);
    }

    private void renderOpponents(Game game) {
        for (PlayerId player : Arrays.asList(PlayerId.LEFT, PlayerId.RIGHT)) {
            Seat seat = session.seatOf(player);
            int cards = game.hand(seat).size();
            String role = cards > 0 ? texts.get("role.withCards", texts.seat(seat), cards) : texts.seat(seat);
            String bubble = game.phase() == Phase.BIDDING ? bubbles.get(player) : null;
            boolean active = game.actingSeat() == seat;
            TableScreen.Team team = team(game, player);
            String teamText = teamText(team);
            screen.setOpponent(player == PlayerId.LEFT, name(player), role, cards, bubble, active, team, teamText);
            if (isOpenHand(game, seat)) {
                List<CardView> open = new ArrayList<>();
                GameType type = game.declaration().type();
                for (HandGrouping.CardGroup group : HandGrouping.group(game.hand(seat), type)) {
                    for (Card card : group.cards()) {
                        open.add(cardView(card, TableScreen.OPEN_CARD));
                    }
                }
                screen.showOpenCards(player == PlayerId.LEFT, open);
            }
        }
    }

    /** In Ouvert games the declarer's cards lie face up once play has started. */
    private static boolean isOpenHand(Game game, Seat seat) {
        Declaration declaration = game.declaration();
        return declaration != null && declaration.isOuvert() && seat == game.declarer()
                && (game.phase() == Phase.PLAYING || game.phase() == Phase.TRICK_COMPLETE);
    }

    /** Returns the side of a computer player as seen by the human player. */
    private TableScreen.Team team(Game game, PlayerId player) {
        if (game.declarer() == null) {
            return TableScreen.Team.NONE;
        }
        PlayerId declarer = session.playerAt(game.declarer());
        if (declarer == PlayerId.HUMAN) {
            return TableScreen.Team.OPPONENT;
        }
        return player == declarer ? TableScreen.Team.DECLARER : TableScreen.Team.PARTNER;
    }

    private String teamText(TableScreen.Team team) {
        switch (team) {
            case DECLARER:
                return texts.get("team.declarer");
            case PARTNER:
                return texts.get("team.partner");
            case OPPONENT:
                return texts.get("team.opponent");
            default:
                return null;
        }
    }

    private void renderCenter(Game game) {
        screen.clearCenter();
        switch (game.phase()) {
            case BIDDING:
            case SKAT_DECISION:
                screen.setSkatBacks(texts.get("table.skat"));
                break;
            case DISCARD:
                if (game.declarer() == humanSeat()) {
                    screen.setDiscardSlots(texts.get("table.discardSlots"), selectedView(0), selectedView(1));
                }
                break;
            case PLAYING:
            case TRICK_COMPLETE:
                renderTrick(game);
                break;
            default:
                break;
        }
    }

    private Node selectedView(int index) {
        return index < discardSelection.size() ? cardView(discardSelection.get(index), TableScreen.TRICK_CARD) : null;
    }

    /** Shows the trick; the labels carry the playing order ("1. Ludwig") once a card lies there. */
    private void renderTrick(Game game) {
        Map<PlayerId, Node> cards = new EnumMap<>(PlayerId.class);
        Map<PlayerId, Integer> order = new EnumMap<>(PlayerId.class);
        Seat seat = game.trickLeader();
        for (Card card : game.currentTrick()) {
            PlayerId player = session.playerAt(seat);
            CardView view = cardView(card, TableScreen.TRICK_CARD);
            view.setRotate(trickTilt(player, game.tricks().size()));
            cards.put(player, view);
            order.put(player, order.size() + 1);
            seat = seat.next();
        }
        boolean humanToPlay = game.phase() == Phase.PLAYING && game.actingSeat() == humanSeat();
        String emptyText = texts.get(humanToPlay ? "table.ownTurn" : "table.ownCard");
        for (PlayerId player : PlayerId.values()) {
            Integer position = order.get(player);
            String label = position == null ? name(player) : texts.get("table.trickOrder", position, name(player));
            screen.setTrickSpot(spot(player), label, cards.get(player), emptyText, humanToPlay);
        }
        if (animated && justPlayed != null && cards.containsKey(justPlayed)) {
            screen.animateArrival(spot(justPlayed));
        }
        justPlayed = null;
    }

    /**
     * A card in the trick lies slightly askew, as if just put down: each player a little to their
     * own side, varying from trick to trick, but never so much that the card is hard to read.
     */
    private static double trickTilt(PlayerId player, int trickNumber) {
        double variation = ((trickNumber * 7 + player.ordinal() * 3) % 5 - 2) * 1.5;
        switch (player) {
            case LEFT:
                return -5 + variation;
            case RIGHT:
                return 5 + variation;
            default:
                return variation;
        }
    }

    private static TableScreen.Spot spot(PlayerId player) {
        switch (player) {
            case LEFT:
                return TableScreen.Spot.LEFT;
            case RIGHT:
                return TableScreen.Spot.RIGHT;
            default:
                return TableScreen.Spot.OWN;
        }
    }

    private void renderHand(Game game) {
        Seat human = humanSeat();
        List<Card> hand = game.phase() == Phase.FINISHED ? Collections.emptyList() : game.hand(human);
        GameType type = game.declaration() == null ? null : game.declaration().type();
        boolean playing = game.phase() == Phase.PLAYING && game.actingSeat() == human;
        boolean discarding = game.phase() == Phase.DISCARD && game.declarer() == human;
        List<Card> playable = playing ? game.playableCards() : Collections.emptyList();
        Card tipCard = playing && tip != null && tip.cards().size() == 1 ? tip.card() : null;

        double cardWidth = hand.size() > GameResult.TRICKS ? TableScreen.SMALL_HAND_CARD : TableScreen.HAND_CARD;
        List<String> labels = new ArrayList<>();
        List<List<CardView>> groups = new ArrayList<>();
        for (HandGrouping.CardGroup group : HandGrouping.group(hand, type)) {
            labels.add(describe.groupLabel(group, type));
            List<CardView> views = new ArrayList<>();
            for (Card card : group.cards()) {
                CardView view = cardView(card, cardWidth);
                if (playing) {
                    boolean canPlay = playable.contains(card);
                    if (card == tipCard) {
                        view.setState(CardView.State.TIP);
                    } else {
                        view.setState(canPlay ? CardView.State.PLAYABLE : CardView.State.DIMMED);
                    }
                    if (canPlay) {
                        makeClickable(view);
                    }
                } else if (discarding) {
                    view.setState(discardSelection.contains(card) ? CardView.State.SELECTED : CardView.State.NORMAL);
                    makeClickable(view);
                }
                views.add(view);
            }
            groups.add(views);
        }
        screen.setHand(labels, groups);
    }

    private void makeClickable(CardView view) {
        view.getStyleClass().add("clickable");
        view.setOnMouseClicked(event -> onCard(view.card()));
    }

    private void renderPanelAndHint(Game game) {
        Seat human = humanSeat();
        Seat acting = game.actingSeat();
        boolean humanActs = acting == human;
        String actingName = acting == null ? "" : name(session.playerAt(acting));
        Node panel = null;
        String hint;
        List<Button> buttons = new ArrayList<>();
        switch (game.phase()) {
            case BIDDING:
                if (humanActs) {
                    panel = biddingPanel(game);
                    hint = texts.get(game.isAskingToSay() ? "hint.bid.say" : "hint.bid.hold", game.pendingBidValue());
                } else {
                    hint = texts.get("hint.thinking", actingName);
                }
                break;
            case SKAT_DECISION:
                if (humanActs) {
                    panel = skatPanel(game);
                    hint = texts.get("hint.skat");
                } else {
                    hint = texts.get("hint.computerWonBidding", actingName, game.bid());
                }
                break;
            case DISCARD:
                if (humanActs) {
                    hint = discardHint();
                    Button discard = button(texts.get("button.discard"), event -> onDiscard(), "go");
                    discard.setDisable(discardSelection.size() != 2);
                    buttons.add(discard);
                    if (!discardSelection.isEmpty()) {
                        buttons.add(button(texts.get("button.clearSelection"), event -> {
                            tip = null;
                            discardSelection.clear();
                            refresh();
                        }));
                    }
                } else {
                    hint = texts.get("hint.computerDiscards", actingName);
                }
                break;
            case DECLARATION:
                if (humanActs) {
                    panel = declarationPanel(game);
                    hint = texts.get("hint.declare", game.bid());
                } else {
                    hint = texts.get("hint.computerDeclares", actingName);
                }
                break;
            case PLAYING:
                hint = playingHint(game, humanActs, actingName);
                break;
            case TRICK_COMPLETE:
                hint = trickHint(game);
                break;
            case FINISHED:
                panel = resultPanel(game);
                hint = nextGameHint();
                break;
            default:
                panel = passedInPanel();
                hint = nextGameHint();
                break;
        }
        if (humanActs) {
            buttons.add(button(texts.get("button.tip"), event -> onTip()));
            if (tip != null) {
                hint = tipText(game);
            }
        }
        buttons.add(button(texts.get("button.why"), event -> onWhy()));
        if (game.phase() == Phase.PLAYING || game.phase() == Phase.TRICK_COMPLETE) {
            addPlayButtons(game, buttons);
        }
        screen.setPanel(panel);
        screen.setHint(hint, buttons);
    }

    /** Returns the tip as a sentence, e.g. "Tipp: Leg das Rot-Ass. Ein Ass bringt oft gleich 11 Augen." */
    private String tipText(Game game) {
        String action;
        switch (game.phase()) {
            case BIDDING:
                if (!tip.accept()) {
                    action = texts.get("tip.bid.pass");
                } else {
                    action = texts.get(game.isAskingToSay() ? "tip.bid.say" : "tip.bid.hold", game.pendingBidValue());
                }
                break;
            case SKAT_DECISION:
                action = texts.get(tip.accept() ? "tip.skat.take" : "tip.skat.hand");
                break;
            case DISCARD:
                action = texts.get("tip.discard", texts.cardWithArticle(tip.cards().get(0)),
                        texts.cardWithArticle(tip.cards().get(1)));
                break;
            case DECLARATION:
                action = texts.get("tip.declare", describe.gameName(tip.declaration()));
                break;
            default:
                action = texts.get("tip.play", texts.cardWithArticle(tip.card()));
                break;
        }
        String partner = "";
        if (game.declarer() != null && session.playerAt(game.declarer()) != PlayerId.HUMAN) {
            partner = name(partnerOfHuman(session.playerAt(game.declarer())));
        }
        return action + " " + describe.tipReason(tip, partner);
    }

    /** Marks the button the tip suggests with a glow. */
    private void markTip(Button button, boolean suggested) {
        if (tip != null && suggested) {
            button.getStyleClass().add("tip");
        }
    }

    /** Buttons that help while playing: take back the own card and look at the last trick. */
    private void addPlayButtons(Game game, List<Button> buttons) {
        if (game.canTakeBack(humanSeat())) {
            buttons.add(button(texts.get("button.takeBack"), event -> onTakeBack()));
        }
        if (!game.tricks().isEmpty()) {
            buttons.add(button(texts.get("button.lastTrick"), event -> onLastTrick()));
        }
    }

    private String playingHint(Game game, boolean humanActs, String actingName) {
        String prefix = "";
        if (game.tricks().isEmpty() && game.currentTrick().isEmpty()) {
            PlayerId declarer = session.playerAt(game.declarer());
            String gameName = describe.gameName(game.declaration());
            prefix = declarer == PlayerId.HUMAN ? texts.get("hint.youPlay", gameName) + " "
                    : texts.get("hint.declarerPlays", name(declarer), gameName, name(partnerOfHuman(declarer))) + " ";
        }
        if (!humanActs) {
            return prefix + texts.get("hint.turn", actingName);
        }
        return prefix + texts.get("hint.yourTurn") + " "
                + describe.playHint(game.hand(humanSeat()), game.currentTrick(), game.declaration().type());
    }

    private String trickHint(Game game) {
        PlayerId winner = session.playerAt(game.currentTrickWinner());
        String sum = describe.trickSum(game.currentTrick());
        if (winner == PlayerId.HUMAN) {
            return texts.get("hint.trick.you", sum);
        }
        return texts.get("hint.trick.other", name(winner), sum);
    }

    private String discardHint() {
        if (discardSelection.size() < 2) {
            return texts.get("hint.discard.choose");
        }
        String first = texts.card(discardSelection.get(0));
        String second = texts.card(discardSelection.get(1));
        return texts.get("hint.discard.selected", first, second, Card.totalPoints(discardSelection));
    }

    private String nextGameHint() {
        PlayerId nextDealer = session.dealer().next();
        return nextDealer == PlayerId.HUMAN ? texts.get("hint.nextGame.you")
                : texts.get("hint.nextGame.other", name(nextDealer));
    }

    // ----------------------------------------------------------------------------------------
    // Panels

    private Node biddingPanel(Game game) {
        int value = game.pendingBidValue();
        String situation;
        String question;
        Button yes;
        if (game.isAskingToSay()) {
            situation = game.currentBid() == 0 ? texts.get("bid.nobodyYet")
                    : texts.get("bid.current", game.currentBid());
            question = texts.get("bid.question.say", value);
            yes = value > 0 ? button(texts.get("button.say", value), event -> onBid(true), "go", "big") : null;
        } else {
            situation = texts.get("bid.says", name(lastSayer), value);
            question = texts.get("bid.question.hold", value);
            yes = button(texts.get("button.hold"), event -> onBid(true), "go", "big");
        }
        HBox answers = new HBox(18);
        answers.setAlignment(Pos.CENTER);
        if (yes != null) {
            markTip(yes, tip != null && tip.accept());
            answers.getChildren().add(yes);
        }
        Button pass = button(texts.get("button.pass"), event -> onBid(false), "no", "big");
        markTip(pass, tip != null && !tip.accept());
        answers.getChildren().add(pass);

        String help = biddingHelp(game.hand(humanSeat()));
        return panel(500, label(situation, "panel-text"), label(question, "panel-title"), answers, helpBox(help));
    }

    /** The bidding help; a value that only holds without the skat says so. */
    private String biddingHelp(List<Card> hand) {
        int withSkat = HandEvaluator.maxBid(hand);
        int withHand = HandEvaluator.maxBidWithHand(hand);
        if (withHand > withSkat) {
            return withSkat > 0 ? texts.get("bid.help.reachHand", withHand, withSkat)
                    : texts.get("bid.help.onlyHand", withHand);
        }
        return withSkat > 0 ? texts.get("bid.help.reach", withSkat) : texts.get("bid.help.weak");
    }

    private Node skatPanel(Game game) {
        Button take = button(texts.get("button.takeSkat"), event -> onTakeSkat(), "go", "big");
        Button hand = button(texts.get("button.playHand"), event -> onPlayHand(), "big");
        markTip(take, tip != null && tip.accept());
        markTip(hand, tip != null && !tip.accept());
        HBox answers = new HBox(18, take, hand);
        answers.setAlignment(Pos.CENTER);
        return panel(520, label(texts.get("skat.title"), "panel-title"),
                label(texts.get("skat.question", game.bid()), "panel-text"), answers);
    }

    private Node declarationPanel(Game game) {
        List<Card> cards = new ArrayList<>(game.hand(humanSeat()));
        cards.addAll(game.isHand() ? Collections.emptyList() : game.skat());
        Label title = label(texts.get("declare.title"), "panel-heading");
        Label bid = label(texts.get("top.bid", game.bid()), "panel-note");
        bid.setWrapText(false);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, title, spacer, bid);
        header.setAlignment(Pos.BASELINE_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);
        GameType[] types = {GameType.ACORNS, GameType.LEAVES, GameType.HEARTS, GameType.BELLS, GameType.GRAND,
            GameType.NULL};
        for (int i = 0; i < types.length; i++) {
            grid.add(gameButton(game, cards, types[i]), i % 3, i / 3);
        }
        return panel(560, header, grid, announcementsBox(game));
    }

    /** The "Weitere Ansagen" part: a single button at first, the three announcements after a click. */
    private Node announcementsBox(Game game) {
        if (!showAnnouncements) {
            Button more = button(texts.get("declare.more"), event -> onMoreAnnouncements());
            HBox row = new HBox(more);
            row.setAlignment(Pos.CENTER_LEFT);
            return row;
        }
        boolean hand = game.isHand();
        Announcements chosen = announcements;
        HBox toggles = new HBox(10, label(texts.get("declare.moreTitle"), "panel-text"),
                toggle(texts.get("declare.schneider"), chosen.isSchneider(), hand, chosen::toggleSchneider),
                toggle(texts.get("declare.schwarz"), chosen.isSchwarz(), hand, chosen::toggleSchwarz),
                toggle(texts.get("declare.ouvert"), chosen.isOuvert(), true, chosen::toggleOuvert));
        toggles.setAlignment(Pos.CENTER);
        String note;
        if (!hand) {
            note = texts.get("declare.onlyHand");
        } else if (announcements.isOuvert()) {
            note = texts.get("declare.ouvertNote");
        } else {
            note = texts.get("declare.handNote");
        }
        VBox box = new VBox(6, toggles, label(note, "panel-note"));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Button toggle(String text, boolean on, boolean enabled, Runnable change) {
        Button toggle = button(text, event -> {
            change.run();
            refresh();
        }, "toggle");
        if (on) {
            toggle.getStyleClass().add("on");
        }
        toggle.setDisable(!enabled);
        return toggle;
    }

    private Button gameButton(Game game, List<Card> cards, GameType type) {
        Declaration declaration = announcements.declaration(type, game.isHand());
        Label name = label(type.isSuitGame() ? texts.suit(type.trumpSuit()) : texts.game(type), "game-name");
        name.setWrapText(false);
        name.setGraphic(gameSymbol(type));
        name.setContentDisplay(ContentDisplay.LEFT);
        name.setGraphicTextGap(8);
        Label valueLabel;
        boolean reaches = false;
        if (declaration == null) {
            valueLabel = label(texts.get("declare.notPossible"), "game-value");
        } else {
            int value = HandEvaluator.value(cards, declaration);
            reaches = value >= game.bid();
            valueLabel = label(reaches ? texts.get("declare.value", value) : texts.get("declare.tooLow", value),
                    "game-value");
        }
        VBox content = new VBox(4, name, valueLabel);
        content.setAlignment(Pos.CENTER);
        Button button = new Button();
        button.setGraphic(content);
        button.getStyleClass().add("game-button");
        if (!reaches) {
            button.getStyleClass().add("weak");
        }
        button.setDisable(declaration == null || !game.canDeclare(declaration));
        button.setOnAction(event -> onDeclare(declaration));
        markTip(button, tip != null && declaration != null && declaration.equals(tip.declaration()));
        return button;
    }

    private Node gameSymbol(GameType type) {
        if (type.isSuitGame()) {
            return SuitSymbol.create(type.trumpSuit(), 0, 0, 28, settings.deck().isFrench());
        }
        if (type == GameType.GRAND) {
            GridPane four = new GridPane();
            four.setHgap(1);
            four.setVgap(1);
            Suit[] suits = Suit.values();
            for (int i = 0; i < suits.length; i++) {
                four.add(SuitSymbol.create(suits[i], 0, 0, 14, settings.deck().isFrench()), i % 2, i / 2);
            }
            return four;
        }
        return label("0", "null-symbol");
    }

    /** Tells whether the human player's side won the finished game. */
    private boolean humanWon(Game game) {
        boolean declarerWon = game.result().isWon();
        return session.playerAt(game.declarer()) == PlayerId.HUMAN ? declarerWon : !declarerWon;
    }

    private Node resultPanel(Game game) {
        GameResult result = game.result();
        PlayerId declarer = session.playerAt(game.declarer());
        Label title = humanWon(game)
                ? label(texts.get("result.won"), "result-won")
                : label(texts.get("result.lost"), "result-lost");
        String gameName = describe.gameName(result.declaration());
        String outcome = declarer == PlayerId.HUMAN
                ? texts.get(result.isWon() ? "result.you.won" : "result.you.lost", gameName)
                : texts.get(result.isWon() ? "result.other.won" : "result.other.lost", name(declarer), gameName);
        List<Node> lines = new ArrayList<>();
        lines.add(title);
        lines.add(label(outcome, "panel-text"));
        lines.add(label(describe.calculation(result), "panel-text"));
        if (result.declaration().type() != GameType.NULL) {
            lines.add(label(texts.get("result.points", result.declarerPoints(), TOTAL_POINTS - result.declarerPoints()),
                    "panel-note"));
        }
        HBox next = new HBox(18, button(texts.get("button.nextGame"), event -> onNextGame(), "go", "big"),
                button(texts.get("button.allTricks"), event -> onAllTricks(), "big"));
        next.setAlignment(Pos.CENTER);
        lines.add(next);
        return panel(600, lines.toArray(new Node[0]));
    }

    /** All tricks of the game in playing order, three per row, and the skat at the end. */
    private Node allTricksPanel() {
        Game game = game();
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        List<CompletedTrick> tricks = game.tricks();
        for (int i = 0; i < tricks.size(); i++) {
            CompletedTrick trick = tricks.get(i);
            PlayerId winner = session.playerAt(trick.winner());
            String text = winner == PlayerId.HUMAN ? texts.get("allTricks.you", i + 1)
                    : texts.get("allTricks.other", i + 1, name(winner));
            grid.add(cardRow(text, trick.cards()), i % ALL_TRICKS_COLUMNS, i / ALL_TRICKS_COLUMNS);
        }
        String skat = texts.get("allTricks.skat", Card.totalPoints(game.skat()));
        int index = tricks.size();
        grid.add(cardRow(skat, game.skat()), index % ALL_TRICKS_COLUMNS, index / ALL_TRICKS_COLUMNS);
        return panel(820, label(texts.get("allTricks.title"), "panel-title"), grid, closeButton());
    }

    private Node cardRow(String text, List<Card> cards) {
        HBox row = new HBox(4);
        for (Card card : cards) {
            row.getChildren().add(cardView(card, SMALL_CARD));
        }
        return new VBox(4, label(text, "panel-note"), row);
    }

    // ----------------------------------------------------------------------------------------
    // Menu and windows in front of the table

    private void showOverlay(Node node, Pos position) {
        overlay = node;
        overlayPosition = position;
        refresh();
    }

    void closeOverlay() {
        overlay = null;
        refresh();
    }

    private Node menuPanel() {
        List<Node> items = new ArrayList<>();
        items.add(menuItem(texts.get("menu.newGame"), event -> {
            if (session.isGameRunning()) {
                showOverlay(confirmPanel(texts.get("confirm.newGame.title"), texts.get("confirm.newGame.text"),
                        texts.get("confirm.newGame.yes"), this::newGame), Pos.CENTER);
            } else {
                newGame();
            }
        }));
        if (!game().tricks().isEmpty()) {
            items.add(menuItem(texts.get("menu.lastTrick"), event -> onLastTrick()));
        }
        items.add(menuItem(texts.get("menu.settings"), event -> onSettings()));
        items.add(menuItem(texts.get("menu.rules"), event -> onRules()));
        items.add(menuItem(texts.get("menu.quit"), event -> showOverlay(confirmPanel(texts.get("confirm.quit.title"),
                session.isGameRunning() ? texts.get("confirm.quit.text") : "", texts.get("confirm.quit.yes"),
                Platform::exit), Pos.CENTER)));
        items.add(menuItem(texts.get("menu.back"), event -> closeOverlay()));
        VBox menu = new VBox(8);
        menu.getChildren().addAll(items);
        menu.getStyleClass().add("menu-panel");
        menu.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return menu;
    }

    /** The settings window: strength of the computer players and sound; changes count at once. */
    private Node settingsPanel() {
        VBox strengths = new VBox(8);
        for (Strength strength : Strength.values()) {
            // Wrapping labels inside a button are measured too tall, so these short texts stay on one line.
            Label name = label(texts.get("strength." + strength.name()), "panel-heading");
            Label note = label(texts.get("strength.text." + strength.name()), "panel-note");
            name.setWrapText(false);
            note.setWrapText(false);
            VBox text = new VBox(2, name, note);
            Button choice = new Button();
            choice.setGraphic(text);
            choice.getStyleClass().add("strength-choice");
            if (strength == settings.strength()) {
                choice.getStyleClass().add("on");
            }
            choice.setMaxWidth(Double.MAX_VALUE);
            choice.setOnAction(event -> {
                settings.setStrength(strength);
                computer = strength.create(seeds.nextLong());
                onSettings();
            });
            strengths.getChildren().add(choice);
        }
        Button sound = button(texts.get(settings.isSoundOn() ? "sound.on" : "sound.off"), event -> {
            settings.setSoundOn(!settings.isSoundOn());
            onSettings();
        }, "toggle");
        if (settings.isSoundOn()) {
            sound.getStyleClass().add("on");
        }
        HBox soundRow = new HBox(14, label(texts.get("settings.sound"), "panel-heading"), sound);
        soundRow.setAlignment(Pos.CENTER_LEFT);
        VBox panel = panel(640, label(texts.get("settings.title"), "panel-title"),
                label(texts.get("settings.strength"), "panel-heading"), strengths,
                label(texts.get("settings.deck"), "panel-heading"), deckChoice(), soundRow, closeButton());
        panel.setAlignment(Pos.TOP_LEFT);
        return panel;
    }

    /** One button per card deck with an Ober and an ace as samples; the chosen deck is framed green. */
    private Node deckChoice() {
        HBox decks = new HBox(10);
        for (CardDeck deck : CardDeck.values()) {
            HBox samples = new HBox(4,
                    new CardView(Card.of(Suit.LEAVES, Rank.OBER), SAMPLE_CARD, texts, deck),
                    new CardView(Card.of(Suit.HEARTS, Rank.ACE), SAMPLE_CARD, texts, deck));
            samples.setAlignment(Pos.CENTER);
            Label name = label(texts.get("deck." + deck.name()), "panel-text");
            name.setWrapText(false);
            VBox content = new VBox(6, samples, name);
            content.setAlignment(Pos.CENTER);
            Button choice = new Button();
            choice.setGraphic(content);
            choice.getStyleClass().add("strength-choice");
            if (deck == settings.deck()) {
                choice.getStyleClass().add("on");
            }
            choice.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(choice, Priority.ALWAYS);
            choice.setOnAction(event -> {
                settings.setDeck(deck);
                useDeck(deck);
                onSettings();
            });
            decks.getChildren().add(choice);
        }
        return decks;
    }

    /** Draws the cards of {@code deck} and names them accordingly from now on. */
    private void useDeck(CardDeck deck) {
        texts.setFrench(deck.isFrench());
        screen.setDeck(deck);
    }

    private static Button menuItem(String text, EventHandler<ActionEvent> action) {
        Button item = button(text, action, "menu-item");
        item.setMaxWidth(Double.MAX_VALUE);
        return item;
    }

    private Node confirmPanel(String title, String text, String yes, Runnable action) {
        HBox answers = new HBox(18,
                button(yes, event -> action.run(), "go", "big"),
                button(texts.get("confirm.keepPlaying"), event -> closeOverlay(), "big"));
        answers.setAlignment(Pos.CENTER);
        List<Node> lines = new ArrayList<>();
        lines.add(label(title, "panel-title"));
        if (!text.isEmpty()) {
            lines.add(label(text, "panel-text"));
        }
        lines.add(answers);
        return panel(520, lines.toArray(new Node[0]));
    }

    private Node lastTrickPanel() {
        List<CompletedTrick> tricks = game().tricks();
        if (tricks.isEmpty()) {
            return panel(520, label(texts.get("lastTrick.title"), "panel-title"),
                    label(texts.get("lastTrick.none"), "panel-text"), closeButton());
        }
        CompletedTrick trick = tricks.get(tricks.size() - 1);
        HBox cards = new HBox(24);
        cards.setAlignment(Pos.CENTER);
        for (int i = 0; i < trick.cards().size(); i++) {
            Label who = label(texts.get("table.trickOrder", i + 1, name(session.playerAt(trick.playerOf(i)))),
                    "panel-note");
            VBox column = new VBox(6, who, cardView(trick.cards().get(i), TableScreen.TRICK_CARD));
            column.setAlignment(Pos.CENTER);
            cards.getChildren().add(column);
        }
        PlayerId winner = session.playerAt(trick.winner());
        String sum = describe.trickSum(trick.cards());
        String text = winner == PlayerId.HUMAN ? texts.get("lastTrick.you", sum)
                : texts.get("lastTrick.other", name(winner), sum);
        return panel(620, label(texts.get("lastTrick.title"), "panel-title"), cards, label(text, "panel-text"),
                closeButton());
    }

    private Node closeButton() {
        HBox row = new HBox(button(texts.get("button.close"), event -> closeOverlay(), "go", "big"));
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private Node passedInPanel() {
        HBox next = new HBox(button(texts.get("button.nextGame"), event -> onNextGame(), "go", "big"));
        next.setAlignment(Pos.CENTER);
        return panel(520, label(texts.get("passedIn.title"), "panel-title"),
                label(texts.get("passedIn.text"), "panel-text"), next);
    }

    // ----------------------------------------------------------------------------------------
    // Small helpers

    private String name(PlayerId player) {
        return texts.player(player);
    }

    private CardView cardView(Card card, double width) {
        return new CardView(card, width, texts, settings.deck());
    }

    private static VBox panel(double width, Node... children) {
        VBox panel = new VBox(12, children);
        panel.getStyleClass().add("panel");
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(width);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        return panel;
    }

    private static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    private static Node helpBox(String text) {
        Label icon = new Label("?");
        icon.getStyleClass().add("help-icon");
        Label sentence = new Label(text);
        sentence.setWrapText(true);
        HBox box = new HBox(10, icon, sentence);
        box.getStyleClass().add("help-box");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Button button(String text, EventHandler<ActionEvent> action, String... styleClasses) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClasses);
        button.setOnAction(action);
        return button;
    }
}
