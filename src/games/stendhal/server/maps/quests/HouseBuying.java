package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;
import games.stendhal.server.entity.npc.parser.JokerExprMatcher;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Controls house buying.
 *
 * @author kymara
 */

public class HouseBuying extends AbstractQuest {

	// constants
	private static final String QUEST_SLOT = "house";

	private static final String PRINCESS_QUEST_SLOT = "imperial_princess";
	private static final String ANNA_QUEST_SLOT = "toys_collector";
	private static final String KEYRING_QUEST_SLOT = "hungry_joshua";
	private static final String GHOSTS_QUEST_SLOT = "find_ghosts";
	private static final String DAILY_ITEM_QUEST_SLOT = "daily_item";
	private static final String FISHROD_QUEST_SLOT = "get_fishing_rod";
	private static final String ZARA_QUEST_SLOT = "suntan_cream_zara";
	private static final String KIRDNEH_QUEST_SLOT = "weekly_item";

	// Cost to buy house (lots!)
	private static final int COST = 100000;
	private static final int COST_ADOS = 120000;
	private static final int COST_KIRDNEH = 120000;

	// Cost to buy spare keys
	private static final int COST_OF_SPARE_KEY = 1000;

	/*
	 * age required to buy a house. Note, age is in minutes, not seconds! So
	 * this is 300 hours
	 */
	private static final int REQUIRED_AGE = 300 * 60;

	/*
	 * POSTMAN_STORAGE_SLOT_1 is the initial postman quest slot. It would be around 75 long
	 * when all 25 houses in Kalavan full. As more houses get added (in other zones?) then we
	 * must always make sure this postman quest slot stays under 255 characters
	 * There are 18 Ados houses, taking us to 129 in house slot. 
	 * So we worry about the limit and put it to a new one, ados_house
	 */
	private static final String POSTMAN_SLOT_INIT = ";";
	private static final String POSTMAN_STORAGE_SLOT_1 = "house";
	private static final String POSTMAN_STORAGE_SLOT_2 = "ados_house";
	private static final String POSTMAN_STORAGE_SLOT_3 = "kirdneh_house";
	
	private static final String ZONE_NAME = "0_kalavan_city";
	private static final String ZONE_NAME2 = "int_ados_town_hall_3";
	private static final String ZONE_NAME3 = "int_kirdneh_townhall";

	protected SpeakerNPC npc;
	protected SpeakerNPC npc2;
	protected SpeakerNPC npc3;

	protected StendhalRPZone zone;
	protected StendhalRPZone zone2;
	protected StendhalRPZone zone3;

	@Override
	public void init(final String name) {
		super.init(name);
	}
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void createNPC() {
		npc = new SpeakerNPC("Barrett Holmes") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(55, 94));
				nodes.add(new Node(93, 94));
				nodes.add(new Node(93, 73));
				nodes.add(new Node(107, 73));
				nodes.add(new Node(107, 35));
				nodes.add(new Node(84, 35));
				nodes.add(new Node(84, 20));
				nodes.add(new Node(17, 20));
				nodes.add(new Node(17, 82));
				nodes.add(new Node(43, 82));
				nodes.add(new Node(43, 94));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						String reply;
						if (player.hasQuest(QUEST_SLOT)) {
							reply = " At the cost of "
									+ COST_OF_SPARE_KEY
									+ " money you can purchase a spare key for your house. Do you want to buy one now?";
							engine.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							reply = "";
						}
						engine.say("Hello, " + player.getTitle() + "." + reply);
					}
				});

				addReply("cost", null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						if (player.getAge() < REQUIRED_AGE) {
							engine.say("The cost of a new house is "
									+ COST
									+ " money. But I am afraid I cannot trust you with house ownership just yet, as you have not been a part of this world long enough.");
						} else if (!player.isQuestCompleted(PRINCESS_QUEST_SLOT)) {
							engine.say("The cost of a new house is "
									+ COST
									+ " money. But I am afraid I cannot sell you a house until your citizenship has been approved by the King, who you will find north of here in Kalavan Castle. Try speaking to his daughter first, she is ... friendlier.");
						} else if (!player.hasQuest(QUEST_SLOT)) {
							engine.say("The cost of a new house is "
									+ COST
									+ " money. If you have a house in mind, please tell me the number now. I will check availability.");
							engine.setCurrentState(ConversationStates.QUEST_OFFERED);
						} else {
							engine.say("As you already know, the cost of a new house is "
									+ COST
									+ " money. But you cannot own more than one house, the market is too demanding for that!");
						}
					}
				});

				// handle house numbers 1 to 25
				add(ConversationStates.QUEST_OFFERED,
					// match for all numbers as trigger expression
					"NUM", new JokerExprMatcher(),
					new TextHasNumberCondition(1, 25),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
							final Player postman = SingletonRepository.getRuleProcessor().getPlayer(
									"postman");
							// is postman online?
							if (postman != null) {
								// First, check if anyone has bought a
								// house from this NPC yet
								if (!postman.hasQuest(POSTMAN_STORAGE_SLOT_1)) {
									postman.setQuest(POSTMAN_STORAGE_SLOT_1, POSTMAN_SLOT_INIT);
								}
								final String postmanslot = postman.getQuest(QUEST_SLOT);
								final String[] boughthouses = postmanslot.split(";");
								final List<String> doneList = Arrays.asList(boughthouses);
								final String itemName = Integer.toString(sentence.getNumeral().getAmount());
								// now check if the house they said is free
								if (!doneList.contains(itemName)) {
									// it's available, so take money
									if (player.isEquipped("money", COST)) {
										final Item key = SingletonRepository.getEntityManager().getItem(
												"private key " + itemName);
										engine.say("Congratulations, here is your key to house "
												+ itemName
												+ "! Do you want to buy a spare key, at a price of "
												+ COST_OF_SPARE_KEY + " money?");
										key.setUndroppableOnDeath(true);
										if (player.equip(key)) {
											player.drop("money", COST);
											// remember what house they own
											player.setQuest(QUEST_SLOT, itemName);
											postman.setQuest(QUEST_SLOT, postmanslot + ";" + itemName);
											engine.setCurrentState(ConversationStates.QUESTION_1);
										} else {
											engine.say("Sorry, you can't carry more keys!");
										}
									} else {
										engine.say("You do not have enough money to buy a house!");
									}
								} else {
									engine.say("Sorry, house " + itemName
											+ " is sold, please give me the number of another.");
									engine.setCurrentState(ConversationStates.QUEST_OFFERED);
								}
							} else {
								// postman is offline!
								engine.say("Oh dear, I've lost my records temporarily. I'm afraid I can't check anything for you. Please try again another time.");
							}
						}
					});

				// we need to warn people who buy spare keys about the house
				// being accessible to other players with a key
				add(ConversationStates.QUESTION_1,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.QUESTION_2,
					"Before we go on, I must warn you that anyone with a key to your house can enter it, and have access to any creature you left inside, whenever they like. Do you still wish to buy a spare key?",
					null);

				// player wants spare keys and is OK with house being accessible
				// to other person.
				add(ConversationStates.QUESTION_2,
					ConversationPhrases.YES_MESSAGES, 
					null,
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
							if (player.isEquipped("money", COST_OF_SPARE_KEY)) {
								final String house = player.getQuest(QUEST_SLOT);
								final Item key = SingletonRepository.getEntityManager().getItem(
										"private key " + house);
								key.setUndroppableOnDeath(true);
								if (player.equip(key)) {
									player.drop("money", COST_OF_SPARE_KEY);
									engine.say("Here you go, a spare key to your house. Please remember, only give spare keys to people you #really, #really, trust!");
								} else {
									engine.say("Sorry, you can't carry more keys!");
								}
							} else {
								engine.say("You do not have enough money for another key!");
							}
						}
					});

				add(ConversationStates.QUESTION_2,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"That is wise of you. It is certainly better to restrict use of your house to those you can really trust.",
					null);
				add(ConversationStates.QUESTION_1,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"No problem! If I can help you with anything else, just ask.",
					null);
				addJob("I'm an estate agent. In simple terms, I sell houses to those who have been granted #citizenship. They #cost a lot, of course. Our brochure is at #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
				addReply("citizenship",
						"The royalty in Kalavan Castle decide that.");
				addReply(
						"buy",
						"You should really enquire the #cost before you ask to buy. And check our brochure, #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
				addReply("really",
						"That's right, really, really, really. Really.");
				addOffer("I sell houses, please look at #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses for examples of how they look inside. Then ask about the #cost when you are ready.");
				addHelp("You may be eligible to buy a house if there are any available. If you can pay the #cost, I'll give you a key. As a house owner you can buy spare keys to give your friends. See #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses for pictures inside the houses and more details.");
				addQuest("You may buy houses from me, please ask the #cost if you are interested. Perhaps you would first like to view our brochure, #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
				addGoodbye("Goodbye.");
			}
		};

		npc.setDescription("You see a smart looking man.");
		npc.setEntityClass("estateagentnpc");
		npc.setPosition(55, 94);
		npc.initHP(100);
		zone.add(npc);
	}
	
	private void createNPC2() {
		npc2 = new SpeakerNPC("Reg Denson") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(37, 13));
				nodes.add(new Node(31, 13));
				nodes.add(new Node(31, 10));
				nodes.add(new Node(35, 10));
				nodes.add(new Node(35, 4));
				nodes.add(new Node(25, 4));
				nodes.add(new Node(25, 15));
				nodes.add(new Node(15, 15));
				nodes.add(new Node(15, 9));
				nodes.add(new Node(18, 9));
				nodes.add(new Node(18, 4));
				nodes.add(new Node(18, 10));
				nodes.add(new Node(15, 10));
				nodes.add(new Node(15, 16));
				nodes.add(new Node(25, 16));
				nodes.add(new Node(25, 3));
				nodes.add(new Node(35, 3));
				nodes.add(new Node(35, 10));
				nodes.add(new Node(37, 10));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						String reply;
						if (player.hasQuest(QUEST_SLOT)) {
							reply = " At the cost of "
									+ COST_OF_SPARE_KEY
									+ " money you can purchase a spare key for your house. Do you want to buy one now?";
							engine.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							reply = "";
						}
						engine.say("Hello, " + player.getTitle() + "." + reply);
					}
				});

				addReply("cost", null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						if (player.getAge() < REQUIRED_AGE) {
							engine.say("The cost of a new house in Ados is "
									+ COST_ADOS
									+ " money. But I am afraid I cannot trust you with house ownership just yet, as you have not been a part of this world long enough.");
						} else if (!(player.isQuestCompleted(DAILY_ITEM_QUEST_SLOT)
								     && player.isQuestCompleted(ANNA_QUEST_SLOT)
								     && player.isQuestCompleted(KEYRING_QUEST_SLOT)
								     && player.isQuestCompleted(FISHROD_QUEST_SLOT)
								     && player.isQuestCompleted(GHOSTS_QUEST_SLOT)
								     && player.isQuestCompleted(ZARA_QUEST_SLOT))) {
							engine.say("The cost of a new house in Ados is "
									+ COST_ADOS
									+ " money. But I am afraid I cannot sell you a house yet as you must first prove yourself a worthy #citizen.");
						} else if (!player.hasQuest(QUEST_SLOT)) {
							engine.say("The cost of a new house in Ados is "
									+ COST_ADOS
									+ " money. If you have a house in mind, please tell me the number now. I will check availability. The Ados houses are numbered from 50 to 68.");
							engine.setCurrentState(ConversationStates.QUEST_OFFERED);
						} else {
							engine.say("The in Ados cost of a new house is "
									+ COST_ADOS
									+ " money. But you cannot own more than one house on the island, the market is too demanding for that!");
						}
					}
				});

				// handle house numbers 50 to 68
				add(ConversationStates.QUEST_OFFERED,
					// match for all numbers as trigger expression
					"NUM", new JokerExprMatcher(),
					new TextHasNumberCondition(50, 68),
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
							final Player postman = SingletonRepository.getRuleProcessor().getPlayer(
									"postman");
							// is postman online?
							if (postman != null) {
								// First, check if anyone has bought a
								// house from this NPC yet
								if (!postman.hasQuest(POSTMAN_STORAGE_SLOT_2)) {
									postman.setQuest(POSTMAN_STORAGE_SLOT_2, POSTMAN_SLOT_INIT);
								}
								final String postmanslot = postman.getQuest(POSTMAN_STORAGE_SLOT_2);
								final String[] boughthouses = postmanslot.split(";");
								final List<String> doneList = Arrays.asList(boughthouses);
								final String itemName = Integer.toString(sentence.getNumeral().getAmount());
								// now check if the house they said is free
								if (!doneList.contains(itemName)) {
									// it's available, so take money
									if (player.isEquipped("money", COST_ADOS)) {
										final Item key = SingletonRepository.getEntityManager().getItem(
												"private key " + itemName);
										engine.say("Congratulations, here is your key to house "
												+ itemName
												+ "! Do you want to buy a spare key, at a price of "
												+ COST_OF_SPARE_KEY + " money?");
										key.setUndroppableOnDeath(true);
										if (player.equip(key)) {
											player.drop("money", COST_ADOS);
											// remember what house they own
											player.setQuest(QUEST_SLOT, itemName);
											postman.setQuest(POSTMAN_STORAGE_SLOT_2, postmanslot + ";" + itemName);
											engine.setCurrentState(ConversationStates.QUESTION_1);
										} else {
											engine.say("Sorry, you can't carry more keys!");
										}
									} else {
										engine.say("You do not have enough money to buy a house!");
									}
								} else {
									engine.say("Sorry, house " + itemName
											+ " is sold, please give me the number of another.");
									engine.setCurrentState(ConversationStates.QUEST_OFFERED);
								}
							} else {
								// postman is offline!
								engine.say("Oh dear, I've lost my records temporarily. I'm afraid I can't check anything for you. Please try again another time.");
							}
						}
					});

				// we need to warn people who buy spare keys about the house
				// being accessible to other players with a key
				add(ConversationStates.QUESTION_1,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.QUESTION_2,
					"Before we go on, I must warn you that anyone with a key to your house can enter it, and have access to any creature you left inside, whenever they like. Do you still wish to buy a spare key?",
					null);

				// player wants spare keys and is OK with house being accessible
				// to other person.
				add(ConversationStates.QUESTION_2,
					ConversationPhrases.YES_MESSAGES, 
					null,
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
							if (player.isEquipped("money", COST_OF_SPARE_KEY)) {
								final String house = player.getQuest(QUEST_SLOT);
								final Item key = SingletonRepository.getEntityManager().getItem(
										"private key " + house);
								key.setUndroppableOnDeath(true);
								if (player.equip(key)) {
									player.drop("money", COST_OF_SPARE_KEY);
									engine.say("Here you go, a spare key to your house. Please remember, only give spare keys to people you #really, #really, trust!");
								} else {
									engine.say("Sorry, you can't carry more keys!");
								}
							} else {
								engine.say("You do not have enough money for another key!");
							}
						}
					});

				add(ConversationStates.QUESTION_2,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"That is wise of you. It is certainly better to restrict use of your house to those you can really trust.",
					null);

				add(ConversationStates.QUESTION_1,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"No problem! If I can help you with anything else, just ask.",
					null);

				addJob("I'm an estate agent. In simple terms, I sell houses for the city of Ados. Please ask about the #cost if you are interested. Our brochure is at #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
                addReply("citizen", "I conduct an informal survey amongst the Ados residents. If you have helped everyone in Ados, I see no reason why they shouldn't recommend you. I speak with my friend Joshua, the Mayor, the little girl Anna, Pequod the fisherman, Zara, and I even commune with Carena, of the spirit world. Together they give a reliable opinion.");
				addReply("buy",	"You may wish to know the #cost before you buy. Perhaps our brochure, #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses would also be of interest.");
				addReply("really", "That's right, really, really, really. Really.");
				addOffer("I sell Ados houses, please look at #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses for examples of how they look inside. Then ask about the #cost when you are ready.");
				addHelp("You may be eligible to become a #citizen. Of course there must also be houses available in Ados. If you can pay the #cost, I'll give you a key. As a house owner you can buy spare keys to give your friends. See #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses for pictures inside the houses and more details.");
				addQuest("You may buy houses from me, please ask the #cost if you are interested. Perhaps you would first like to view our brochure, #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
				addGoodbye("Goodbye.");
			}
		};

		npc2.setDescription("You see a smart looking man.");
		npc2.setEntityClass("estateagent2npc");
		npc2.setPosition(37, 13);
		npc2.initHP(100);
		zone2.add(npc2);
	}

	private void createNPC3() {
		npc3 = new SpeakerNPC("Roger Frampton") {
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						String reply;
						if (player.hasQuest(QUEST_SLOT)) {
							reply = " At the cost of "
									+ COST_OF_SPARE_KEY
									+ " money you can purchase a spare key for your house. Do you want to buy one now?";
							engine.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							reply = "";
						}
						engine.say("Hello, " + player.getTitle() + "." + reply);
					}
				});

				addReply("cost", null, new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
						if (player.getAge() < REQUIRED_AGE) {
							engine.say("The cost of a new house in Kirdneh is "
									+ COST_KIRDNEH
									+ " money. But I am afraid I cannot trust you with house ownership just yet. Come back when you have spent at least " + Integer.toString((int) (REQUIRED_AGE / 60)) + " hours on Faiumoni.");
						} else if (!player.isQuestCompleted(KIRDNEH_QUEST_SLOT)) {
							engine.say("The cost of a new house in Kirdneh is "
									+ COST_KIRDNEH
									+ " money. But my principle is never to sell a house without establishing first the good #reputation of the prospective buyer.");
						} else if (!player.hasQuest(QUEST_SLOT)) {
							engine.say("The cost of a new house in Kirdneh is "
									+ COST_KIRDNEH
									+ " money. If you have a house in mind, please tell me the number now. I will check availability. The Kirdneh houses are numbered from 26 to 49.");
							engine.setCurrentState(ConversationStates.QUEST_OFFERED);
						} else {
							engine.say("In Kirdneh the cost of a new house is "
									+ COST_KIRDNEH
									+ " money. But you cannot own more than one house on the island, the market is too demanding for that!");
						}
					}
				});

				// handle house numbers 26 to 49
				add(ConversationStates.QUEST_OFFERED,
					// match for all numbers as trigger expression
					"NUM", new JokerExprMatcher(),
					new TextHasNumberCondition(26, 49),
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
							final Player postman = SingletonRepository.getRuleProcessor().getPlayer(
									"postman");
							// is postman online?
							if (postman != null) {
								// First, check if anyone has bought a
								// house from this NPC yet
								if (!postman.hasQuest(POSTMAN_STORAGE_SLOT_3)) {
									postman.setQuest(POSTMAN_STORAGE_SLOT_3, POSTMAN_SLOT_INIT);
								}
								final String postmanslot = postman.getQuest(POSTMAN_STORAGE_SLOT_3);
								final String[] boughthouses = postmanslot.split(";");
								final List<String> doneList = Arrays.asList(boughthouses);
								final String itemName = Integer.toString(sentence.getNumeral().getAmount());
								// now check if the house they said is free
								if (!doneList.contains(itemName)) {
									// it's available, so take money
									if (player.isEquipped("money", COST_KIRDNEH)) {
										final Item key = SingletonRepository.getEntityManager().getItem(
												"private key " + itemName);
										engine.say("Congratulations, here is your key to house "
												+ itemName
												+ "! Do you want to buy a spare key, at a price of "
												+ COST_OF_SPARE_KEY + " money?");
										key.setUndroppableOnDeath(true);
										if (player.equip(key)) {
											player.drop("money", COST_KIRDNEH);
											// remember what house they own
											player.setQuest(QUEST_SLOT, itemName);
											postman.setQuest(POSTMAN_STORAGE_SLOT_3, postmanslot + ";" + itemName);
											engine.setCurrentState(ConversationStates.QUESTION_1);
										} else {
											engine.say("Sorry, you can't carry more keys!");
										}
									} else {
										engine.say("You do not have enough money to buy a house!");
									}
								} else {
									engine.say("Sorry, house " + itemName
											+ " is sold, please give me the number of another, from 26 to 49.");
									engine.setCurrentState(ConversationStates.QUEST_OFFERED);
								}
							} else {
								// postman is offline!
								engine.say("Oh dear, I've lost my records temporarily. I'm afraid I can't check anything for you. Please try again another time.");
							}
						}
					});

				// we need to warn people who buy spare keys about the house
				// being accessible to other players with a key
				add(ConversationStates.QUESTION_1,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.QUESTION_2,
					"Before we go on, I must warn you that anyone with a key to your house can enter it, and have access to any creature you left inside, whenever they like. Do you still wish to buy a spare key?",
					null);

				// player wants spare keys and is OK with house being accessible
				// to other person.
				add(ConversationStates.QUESTION_2,
					ConversationPhrases.YES_MESSAGES, 
					null,
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
							if (player.isEquipped("money", COST_OF_SPARE_KEY)) {
								final String house = player.getQuest(QUEST_SLOT);
								final Item key = SingletonRepository.getEntityManager().getItem(
										"private key " + house);
								key.setUndroppableOnDeath(true);
								if (player.equip(key)) {
									player.drop("money", COST_OF_SPARE_KEY);
									engine.say("Here you go, a spare key to your house. Please remember, only give spare keys to people you #really, #really, trust!");
								} else {
									engine.say("Sorry, you can't carry more keys!");
								}
							} else {
								engine.say("You do not have enough money for another key!");
							}
						}
					});

				add(ConversationStates.QUESTION_2,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"That is wise of you. It is certainly better to restrict use of your house to those you can really trust.",
					null);

				add(ConversationStates.QUESTION_1,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"No problem! If I can help you with anything else, just ask.",
					null);

				addJob("I'm an estate agent. In simple terms, I sell houses for the city of Kirdneh. Please ask about the #cost if you are interested. Our brochure is at #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
                addReply("reputation", "I will ask Hazel about you. Provided you've finished any task she asked you to do for her recently, and haven't left anything unfinished, she will like you.");
				addReply("buy",	"You may wish to know the #cost before you buy. Perhaps our brochure, #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses would also be of interest.");
				addReply("really", "That's right, really, really, really. Really.");
				addOffer("I sell houses from this beautiful city of Kirdneh, please look at #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses for examples of how they look inside. Then ask about the #cost when you are ready.");
				addHelp("You must have a good #reputation if you want to buy an available house in Kirdneh. If you can pay the #cost, I'll give you a key. As a house owner you can buy spare keys to give your friends. See #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses for pictures inside typical houses and more details.");
				addQuest("You may buy houses from me, please ask the #cost if you are interested. Perhaps you would first like to view our brochure, #http://arianne.sourceforge.net/wiki/index.php?title=StendhalHouses.");
				addGoodbye("Goodbye.");
			}
		};

		npc3.setDescription("You see a smart looking man.");
		npc3.setEntityClass("man_004_npc");
		npc3.setPosition(31, 4);
		npc3.initHP(100);
		zone3.add(npc3);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		zone = SingletonRepository.getRPWorld().getZone(ZONE_NAME);
		createNPC();

		zone2 = SingletonRepository.getRPWorld().getZone(ZONE_NAME2);
		createNPC2();

		zone3 = SingletonRepository.getRPWorld().getZone(ZONE_NAME3);
		createNPC3();
	}
}
