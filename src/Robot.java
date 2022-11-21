import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

/**
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
 */

public class Robot {
	private static Environment env;
	private static int posRow;
	private static int posCol;
	private boolean toCleanOrNotToClean;
	private boolean pathFound;
	private Block b;
	private LinkedList<Action> path;
	private static Action priorCommand;
	private Properties props;
	private StanfordCoreNLP pipeline;
	private static List<String> repeat = Arrays.asList("again", "more", "further");
	private static List<String> keywordSearch = Arrays.asList("I think you want me to ", "You might actually ask to ",
			"I assume that you'd like to ", "Based on my understanding, I will ", "I believe you wish to ");
	private static List<String> randomResponses = Arrays.asList(
			"I don't quite get that. Can you clarify it a little bit more?",
			"Please provide more clarity to what I should do.",
			"I am sorry I don't understand your command. May I get your command again.",
			"Please tell me your command again.", "May I have your command one more time?",
			"Would you mind if you could repeat and clarify the command?",
			"It would be really helpful if you could check and provide a command again",
			"Could you give more details on your command?");
	private static List<String> praisesResponses = Arrays.asList("No problem, sir. I am at your service.",
			"Thank you for your kind words. I will continue working my best.", "I appreciate your praises");
	private static String robotName = "John";

	private static List<String> acknowledgement = Arrays.asList("Ja ja.", "Got it.", "Roger that.", "Sure.", "OK.");
	private static List<String> negResponses = Arrays.asList("I am sorry for making a mistake.",
			"Please accept my apology.", "Please forgive my action.");
	private static List<String> posResponses = Arrays.asList("Thanks for your kind words.",
			"Appreciated your compliment.", "I bless you for your kind heart.");
	private static String username = null;
	private Scanner sc;

	private String[] arr = new String[20];
	private int record = 0;
	private LinkedList<Predicate> goals = new LinkedList<Predicate>();
	private LinkedList<Predicate> startState = new LinkedList<Predicate>();
	private String action = "";
	private String order = "";
	private boolean reversed = false;
	private boolean reverse = false;
	private Position bPos;// get block position in bfs, mainly used for bring
	private boolean[][] map;
	private container con;
	private LinkedList<Predicate> goalsForR = new LinkedList<Predicate>();
	private int targetRow = 0;
	private int targetCol = 0;
	private Position posForStore;
	private String topBlock;
	private String curTower;
//	private Block bottomBlock;
	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		this.b = null;
		this.path = new LinkedList<>();
		this.map = new boolean[env.getRows()][env.getCols()];
		priorCommand = Action.DO_NOTHING;
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse,sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public void init() {

	}

	public Block getBlock() {
		return this.b;
	}

	public boolean setBlock(Block b) {
		if (this.b == null) {
			this.b = b;
			return true;
		} else if (b == null) {
			this.b = null;
			return true;
		} else {
			return false;
		}
	}

	public boolean getPathFound() {
		return pathFound;
	}

	public int getPathLength() {
		// TODO: modify this procedure to return the actual path length.
		// You will likely have to track it in some counter.
		return 0;
	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action getAction() {
		Annotation annotation;
		topBlock = "";// for unstack
		curTower="";
		if (path.isEmpty()) {
			if (goals.isEmpty()) {
				System.out.print("> ");
				sc = new Scanner(System.in);
				String name = sc.nextLine();
				name = name.toLowerCase();
				boolean containNumBlockOrPosition = false;
				this.reverse = false;
				char[] chars = name.toCharArray();
				for (char c : chars) {
					if (Character.isDigit(c)) {
						containNumBlockOrPosition = true;
					}
				}
				if (!containNumBlockOrPosition) {
					if (name.equals("u")) {
						return Action.MOVE_UP;
					}
					if (name.equals("d")) {
						return Action.MOVE_DOWN;
					}
					if (name.equals("l")) {
						return Action.MOVE_LEFT;
					}
					if (name.equals("r")) {
						return Action.MOVE_RIGHT;
					}
					if (name.equals("us") || name.contains("unstack")) {
						if (env.getBlock(getPosRow(), getPosCol()) != null) {
							System.out.println("Only 1 block on current tile. Cannot unstack.");
							return Action.DO_NOTHING;
						} else if (env.getBlock(getPosRow(), getPosCol()) == null
								&& env.checkTopBlock(getPosRow(), getPosCol()) == null) {
							System.out.println("No block on current tile. Cannot unstack.");
							return Action.DO_NOTHING;
						} else if (getBlock() != null) {
							System.out.println("Currently holding a block. Cannot unstack.");
							return Action.DO_NOTHING;
						}
						return Action.UNSTACK;
					}
					if (name.equals("s") || name.contains("stack")) {
						if (getBlock() == null) {
							System.out.println("Currently not holding a block. Cannot stack.");
						}
						return Action.STACK;
					}
					if (name.equals("pd") || name.contains("putdown")) {
						if (getBlock() == null) {
							System.out.println("Currently not holding a block. Cannot put down.");
						}
						return Action.PUT_DOWN;
					}
					if (name.equals("pu") || name.contains("pickup") || name.contains("pick up")) {
						if (env.getBlock(getPosRow(), getPosCol()) == null
								&& env.checkTopBlock(getPosRow(), getPosCol()) == null) {
							System.out.println("No block on current tile. Cannot pick up.");
							return Action.DO_NOTHING;
						} else if (env.getBlock(getPosRow(), getPosCol()) == null
								&& env.checkTopBlock(getPosRow(), getPosCol()) != null) {
							System.out
									.println("More than 1 block on current tile. Cannot pick up. Use unstack instead");
							return Action.DO_NOTHING;
						} else if (getBlock() != null) {
							System.out.println("Currently holding a block. Cannot pick up.");
							return Action.DO_NOTHING;
						}
						return Action.PICK_UP;
					}
				}
				if (name.equals("reverse")) {
					reverse = true;
				}
//		    System.out.println(name);
				annotation = new Annotation(name);
				pipeline.annotate(annotation);
				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
				if (sentences != null && !sentences.isEmpty()) {
					CoreMap sentence = sentences.get(0);
					System.out.println(sentence);
					String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

					SemanticGraph graph = sentence
							.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

					graph.prettyPrint();

					if (sentiment.toLowerCase().contains("positive")) {
						System.out.println(randomStatement(posResponses));
//					return Action.DO_NOTHING;
					} else if (sentiment.toLowerCase().contains("negative")) {
						System.out.println(randomStatement(negResponses));
//					return Action.DO_NOTHING;
					}
					List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|UH|VB|CD|IN|JJ|NN");
					arr = new String[20];
					record = 0;
					goals = new LinkedList<Predicate>();
					startState = new LinkedList<Predicate>();
					boolean goal = false;

					for (IndexedWord w : li) {
//		    	  if (w.tag().equals("RB")) {
//		    		  return processRB(w.word());
//		    	  }
//		    	  if (w.tag().equals("UH")) {
//		    		  return processUH(w.word());
//		    	  }
//					System.out.println(w.word());
						if (w.tag().equals("VB")) {
							action = w.word();
						}
						if (w.tag().equals("JJ")) { // could come later than the position may use keyword search instead
							order = w.word();
							if (w.word().equals("reverse")) {
								reverse = true;
							}
							// check action unstack
							if (w.word().equals("unstack")) {
								action = w.word();
							}
						}
						// check action stack
						if (w.tag().equals("NN") && w.word().equals("stack")) {
							action = w.word();
						}
						if (w.tag().equals("CD")) { // either a robotID or a position
							int row = -1, col = -1;
							if (w.word().contains(",")) {
								String[] s = w.word().split(",");
								row = Integer.parseInt(s[0]);
								col = Integer.parseInt(s[1]);
								if (!env.validPos(row, col)) {
									System.out.println("Invalid position");
									return Action.DO_NOTHING;
								}
							} else {
								boolean isSet = false;
								int blockID = Integer.parseInt(w.word());
								if (blockID > env.getNumBlock() || blockID < 0) {
									System.out.println("BlockID don't exist");
									return Action.DO_NOTHING;
								}
								// find position
								for (Block b : env.getBlocks()) {
									if (b.getID() == blockID) {
										row = b.getPosition().getRow();
										col = b.getPosition().getCol();
										isSet = true;
										break;
									}
									while (b.getNextBlock() != null) {
										b = b.getNextBlock();
										if (b.getID() == blockID) {
											row = b.getPosition().getRow();
											col = b.getPosition().getCol();
											isSet = true;
											break;
										}
									}
								}
//								if (isSet) {
//									System.out.println("Set row,col success");
//								} else {
//									System.out.println("cannot set row,col blockID");
//								}

							}
							if (!w.word().contains(",")) { // for robotID
								arr[record++] = w.word();
							}
							if (action.equals("pick")) { // pick up
								if (env.getBlock(row, col) == null && env.checkTopBlock(row, col) == null) {
									System.out.println("No block on tile to pick. Cannot pick up.");
									return Action.DO_NOTHING;
								} else if (env.getBlock(row, col) == null && env.checkTopBlock(row, col) != null) {
									System.out.println(
											"More than 1 block on tile to pick. Cannot pick up. Use unstack instead");
									return Action.DO_NOTHING;
								} else if (getBlock() != null) {
									System.out.println("Currently holding a block. Cannot pick up.");
									return Action.DO_NOTHING;
								}
								processCD(w.word(), true); // find path to block
								if (path.isEmpty() && !this.pathFound) { // bfs can't find path might have walls
									System.out.println("Need help. Cannot get block");
									return Action.DO_NOTHING;
								}
								// if path found
								path.add(Action.PICK_UP);

							} else if (action.equals("unstack")) {
								if (env.getBlock(row, col) != null) {
									System.out.println("Only 1 block on current tile. Cannot unstack.");
									return Action.DO_NOTHING;
								} else if (env.getBlock(row, col) == null && env.checkTopBlock(row, col) == null) {
									System.out.println("No block on tile to unstack. Cannot unstack.");
									return Action.DO_NOTHING;
								} else if (getBlock() != null) {
									System.out.println("Currently holding a block. Cannot unstack.");
									return Action.DO_NOTHING;
								}
								if (w.word().contains(",")) {
									processCD(topBlock, true); // find path to block
									if (path.isEmpty() && !this.pathFound) { // bfs can't find path might have walls
										System.out.println("Need help. Cannot get block");
										return Action.DO_NOTHING;
									}
									path.add(Action.UNSTACK);
								} else {
									if (topBlock.equals("")) {
										topBlock = w.word();
										System.out.println("Set top block");
										continue;
									}
									processCD(topBlock, true); // find path to block
									if (path.isEmpty() && !this.pathFound) { // bfs can't find path might have walls
										System.out.println("Need help. Cannot get block");
										return Action.DO_NOTHING;
									}
									path.add(Action.UNSTACK);
								}

							} else if (action.equals("place") || action.equals("put") || action.equals("stack")) {
								if (getBlock() == null) {
									System.out.println("Not currently holding a block. Cannot put down");
									return Action.DO_NOTHING;
								}
								if (action.equals("place") || action.equals("put")) {
									if (env.getBlock(row, col) != null || env.checkTopBlock(row, col) != null) {
										System.out.println("Use stack instead since there is a block.");
										return Action.DO_NOTHING;
									}
									if (w.word().contains(",")) { // for position
										processCD(w.word(), false); // find path
//										String[] s = w.word().split(","); // extract row and col
										path.add(Action.PUT_DOWN);

									} else { // for robotID
										if (this.b == null) {
											System.out.println("Currently not holding any block");
											return Action.DO_NOTHING;
										}
										if (b.getID() != Integer.parseInt(w.word())) {
											System.out.println("BlockID" + b.getID());
											System.out.println("Not the same block hold");
											return Action.DO_NOTHING;
										}
										if (env.isTarget(row, col)) { // check if there is a block
											path.add(Action.STACK);
										} else {
											path.add(Action.PUT_DOWN);
										}
									}
								} else if (action.equals("place") || action.equals("stack")) {

									if (w.word().contains(",")) { // for position
										processCD(w.word(), false); // find path
//										String[] s = w.word().split(","); // extract row and col
										path.add(Action.STACK);

									} else { // for robotID
										if (topBlock.equals("")) {
											topBlock = w.word();
											continue;
										}
										if (env.getBlock(row, col) == null && env.checkTopBlock(row, col) == null) {
											System.out.println("Use stack instead since there is a block.");
											return Action.DO_NOTHING;
										}
										processCD(w.word(), true);
										if (b.getID() != Integer.parseInt(topBlock)) {
											System.out.println("BlockID" + b.getID());
											System.out.println("Not the same block hold");
											return Action.DO_NOTHING;
										}
										path.add(Action.STACK);

									}

								}

							} else if ((action.equals("make") || action.equals("create") || action.equals("move")||action.equals("build")) && w.word().contains(",")) {
								if(curTower.equals("")) {
									curTower=w.word();
									continue;
								}
								if(action.equals("move")) {
									reverse=true;
								}
								String[] s = w.word().split(",");
								this.targetRow = Integer.parseInt(s[0]);
								this.targetCol = Integer.parseInt(s[1]);
								map[targetRow][targetCol] = true;
								
								int currRow = 0, currCol = 0;
								for (Block b : env.getBlocks()) { //create start state for the entire map (on, ontable, clear only)
									if (b == null) {
										System.out.println("Null block in the map");
										return Action.DO_NOTHING;
									}
									String curr = Integer.toString(b.getID());
									String prev;
									startState.add(new OnTable(curr));
									currRow = b.getPosition().getRow();
									currCol = b.getPosition().getCol();
									while (b.getNextBlock() != null) {
										prev = Integer.toString(b.getID());
										b = b.getNextBlock();
										curr = Integer.toString(b.getID());
										startState.add(new On(curr, prev));
									}
									startState.add(new Clear(curr));
									break;
								}
//								System.out.println("Goals");
								if (!reverse) { //same order, preserve order just have additional goalsForR
									this.reversed = false;
									this.goalsForR = new LinkedList<Predicate>();
									LinkedList<Predicate> tmp = (LinkedList<Predicate>) startState.clone();
									goalsForR.add(new OnTable(tmp.remove(0).toString().split(" ")[1]));
									while (tmp.size() > 1) {
										String[] temp = tmp.remove(0).toString().split(" ");
										goalsForR.add(new On(temp[1], temp[2]));
									}
									goalsForR.add(new Clear(tmp.remove(0).toString().split(" ")[1]));

									tmp = (LinkedList<Predicate>) startState.clone();
									goals.add(new OnTable(tmp.remove(tmp.size() - 1).toString().split(" ")[1]));
									while (tmp.size() > 1) {
										String[] temp = tmp.remove(tmp.size() - 1).toString().split(" ");
										goals.add(new On(temp[2], temp[1]));
									}
									goals.add(new Clear(tmp.remove(tmp.size() - 1).toString().split(" ")[1]));
								} else { //reverse
									LinkedList<Predicate> tmp = (LinkedList<Predicate>) startState.clone();
									goals.add(new OnTable(tmp.remove(tmp.size() - 1).toString().split(" ")[1])); //onTable: 0 split by space and take second data from array
									//swap
									while (tmp.size() > 1) {
										String[] temp = tmp.remove(tmp.size() - 1).toString().split(" ");
										goals.add(new On(temp[2], temp[1]));
									}
									goals.add(new Clear(tmp.remove(tmp.size() - 1).toString().split(" ")[1]));
								}

								Predicate p = goals.removeFirst();
//								System.out.println(p); //those onTable,clear,on print
								con = planHelper(p, new Position(targetRow, targetCol),
										new Position(this.getPosRow(), this.getPosCol())); //true, tar, tar for reverse
								if (!con.b) { //not for reverse
									System.out.println("Goal state unsolvable");
									path = new LinkedList<>();
									goals = new LinkedList<>();
									return Action.DO_NOTHING;
								}

							} else if (action.equals("bring")) {
								if (w.word().contains(",")) { // the same algorithm as put but different in bfs
									String[] s = w.word().split(",");
									Position p2 = new Position(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
									path.addAll(bfs2(bPos, p2)); //add all action after pick up/unstack to put down/stack
									path.add(Action.PUT_DOWN);
									path.add(Action.STACK);
								} else {// the same algorithm as pick
									processCD(w.word(), true); // find path to block
									if (path.isEmpty() && !this.pathFound) { // bfs can't find path might have walls
										System.out.println("Need help. Cannot get block");
										return Action.DO_NOTHING;
									}
									// if path found
									path.add(Action.PICK_UP);
									path.add(Action.UNSTACK);
								}
							}
						}
					}
					return Action.DO_NOTHING;
				}
				System.out.println("Empty sentence.");
				return Action.DO_NOTHING;

			} else { //goal is not empty
				Predicate p = goals.removeFirst();
				System.out.println(p);
				path = new LinkedList<>();
				if (!p.toString().contains("clear")) {
					con = planHelper(p, con.target, con.p);
				}
				if (!con.b) {
					System.out.println("Can't continue");
					path = new LinkedList<>();
					return Action.DO_NOTHING;
				}
			}

		} else { //path is not empty
			return path.remove();
		}

		if (!this.reversed && path.isEmpty() && !goalsForR.isEmpty()) {//check if first reverse is for preserve order
			reversed = true;
			reverse = true;
			goals = (LinkedList<Predicate>) goalsForR.clone();
			Predicate p = goalsForR.removeFirst();
			goals.removeFirst();
			System.out.println(p);
			if(posForStore==null) {
				return Action.DO_NOTHING;
			}
			con = planHelper(p, new Position(this.targetRow, this.targetCol), posForStore); //plan from first to second reverse
			if (!con.b) {
				System.out.println("Can't continue");
				path = new LinkedList<>();
				return Action.DO_NOTHING;
			}
		}
		return Action.DO_NOTHING;
	}

	private class container {
		public boolean b;
		public Position p;
		public Position target;

		container(boolean b, Position p, Position t) {
			this.b = b;
			this.p = p;
			this.target = t;
		}
	}

	private container planHelper(Predicate p, Position tar, Position cur) { //goal predicate
		String[] t = p.toString().split(" "); //ontable: 0
		int targetID = Integer.valueOf(t[1]);
		Block target = null;
		for (Block b : env.getBlocks()) { //find current start state
			while (b != null) {
				if (b.getID() == targetID) {
					target = b;
					break;
				}
				b = b.getNextBlock();
			}
		}

		if (!reverse) {
			boolean available = false;
			int row = 0, col = 0;
			for (int i = tar.getRow(); i < env.getRows(); i++) {
				for (int j = tar.getCol(); j < env.getCols(); j++) {
					if (!map[i][j] && (target.getPosition().getRow() != i || target.getPosition().getCol() != j)) {
						available = true; //available for placing block
						row = i;
						col = j;
						break;
					}
				}
				if (available) {
					break;
				}
			}
			if (!available) {
				return new container(false, null, null);
			}
			if (!this.reversed) { //check if first reverse is done
				posForStore = new Position(row, col); //store the position after first reverse
			}
			path.addAll(bfs2(cur, target.getPosition()));
			path.add(Action.PICK_UP);
			path.add(Action.UNSTACK);
			path.addAll(bfs2(target.getPosition(), posForStore));
			path.add(Action.PUT_DOWN);
			path.add(Action.STACK);
			return new container(true, posForStore, tar);
		}
		//reverse
		path.addAll(bfs2(cur, target.getPosition())); //create path from current robot location to start state block loc
		path.add(Action.PICK_UP);
		path.add(Action.UNSTACK);
		path.addAll(bfs2(target.getPosition(), tar)); //start state block loc to goal loc
		path.add(Action.PUT_DOWN);
		path.add(Action.STACK);

		return new container(true, tar, tar);
	}

	// add boolean for stack
	private boolean processCD(String word, boolean needStack) { // find path
		Position p = null;
		if (!word.contains(",")) {// position
			System.out.println(word);
			LinkedList<Block> blocks = env.getBlocks();
			Block b = blocks.getFirst();
			p = b.getPosition();
			try {
				if (env.checkTopBlock(p.getRow(), p.getCol()).getID() != Integer.parseInt(word) && needStack) {
					System.out.println("Not possible Not top block");
					return false;
				}
			} catch (Exception e) {

			}

		} else { // robotID
			String[] s = word.split(",");
			p = new Position(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

		}

		bfs(p);
//		while(!path.isEmpty()) {
//			path.remove();
//		}
		return true;
	}

	private Action processVB(String word) { // word pick up
		action = word;
		return Action.DO_NOTHING;
	}

	static public Action processRB(String word) {
		System.out.println(word);
		return Action.DO_NOTHING;
	}

	static public Action processUH(String word) {
		System.out.println(word);
		return Action.DO_NOTHING;
	}

//	public Action pickUp(Position p) {
//		targetRow=p.getRow();
//		targetCol=p.getCol();
//		System.out.println("Target at "+targetRow + ","+targetCol);
//		bfs();
//		while(!path.isEmpty()) {
//			return path.remove();
//		}
//		return Action.DO_NOTHING;
//	}
	boolean[][] visited;
	Queue<Integer> rowQ;
	Queue<Integer> colQ;
	int[] adjrow = new int[] { 0, 0, -1, 1 };
	int[] adjcol = new int[] { 1, -1, 0, 0 };
	int curr;
	int next;
	Integer[] prev;

	public List<Action> bfs2(Position p, Position p2) {
		rowQ = new LinkedList<>();
		colQ = new LinkedList<>();
		visited = new boolean[env.getRows()][env.getCols()];
		prev = new Integer[env.getRows() * env.getCols()];
		bfsHelper2(p, p2);
		return path2(p.getRow(), p.getCol(), p2.getRow(), p2.getCol(), prev);
	}

	private void bfsHelper2(Position p, Position p2) {

		rowQ.add(p.getRow());
		colQ.add(p.getCol());
		curr = 1;
		next = 0;
//		openCount++;
		visited[p.getRow()][p.getCol()] = true;

		while (!rowQ.isEmpty()) {
			int r = rowQ.remove();
			int c = colQ.remove();
			if (r == p2.getRow() && c == p2.getCol()) {
				pathFound = true;
				break;
			}
			adjacent(r, c);
			curr--;
			if (curr == 0) {
				curr = next;
				next = 0;
			}
		}
	}

	public void bfs(Position p) {
		rowQ = new LinkedList<>();
		colQ = new LinkedList<>();
		visited = new boolean[env.getRows()][env.getCols()];
		prev = new Integer[env.getRows() * env.getCols()];
		bfsHelper(p);
		bPos = p;
		path(getPosRow(), getPosCol(), p.getRow(), p.getCol(), prev);
	}

	private void bfsHelper(Position p) {

		rowQ.add(getPosRow());
		colQ.add(getPosCol());
		curr = 1;
		next = 0;
//		openCount++;
		visited[getPosRow()][getPosCol()] = true;

		while (!rowQ.isEmpty()) {
			int r = rowQ.remove();
			int c = colQ.remove();
			if (r == p.getRow() && c == p.getCol()) {
				pathFound = true;
				break;
			}
			adjacent(r, c);
			curr--;
			if (curr == 0) {
				curr = next;
				next = 0;
			}
		}
	}

	private List<Action> path2(int startR, int startC, int endR, int endC, Integer[] list) {
		List<Action> result = new ArrayList<>();
		for (Integer i = endR * env.getCols() + endC; i != null; i = list[i]) {
			if (i != null && list[i] != null) {
				int temp = list[i] - i;
				if (temp == env.getCols()) {
					result.add(Action.MOVE_UP);
				} else if (temp + env.getCols() == 0) {
					result.add(Action.MOVE_DOWN);
				} else if (temp + 1 == 0) {
					result.add(Action.MOVE_RIGHT);
				} else if (temp == 1) {
					result.add(Action.MOVE_LEFT);
				}
//				pathLength++;
			}

		}
		Collections.reverse(result);
		return result;
//		int j = 0;
//		while (j < result.size()) {
//			path.add(j, result.get(j));
//			j++;
//		}
	}

	private void path(int startR, int startC, int endR, int endC, Integer[] list) {
		List<Action> result = new ArrayList<>();
		for (Integer i = endR * env.getCols() + endC; i != null; i = list[i]) {
			if (i != null && list[i] != null) {
				int temp = list[i] - i;
				if (temp == env.getCols()) {
					result.add(Action.MOVE_UP);
				} else if (temp + env.getCols() == 0) {
					result.add(Action.MOVE_DOWN);
				} else if (temp + 1 == 0) {
					result.add(Action.MOVE_RIGHT);
				} else if (temp == 1) {
					result.add(Action.MOVE_LEFT);
				}
//				pathLength++;
			}

		}
		Collections.reverse(result);
		int j = 0;
		while (j < result.size()) {
			path.add(j, result.get(j));
			j++;
		}

	}

	private void adjacent(int row, int col) {
		int i = 0;
		int r, c;
		while (i <= 3) {
			r = row + adjrow[i];
			c = col + adjcol[i];
			i++;
			if (r < 0 || r >= env.getRows() || c < 0 || c >= env.getCols() || visited[r][c] == true
					|| env.getTileStatus(r, c) == TileStatus.IMPASSABLE) {
				continue;
			}
			rowQ.add(r);
			colQ.add(c);
//			openCount++;
			visited[r][c] = true;
			next++;
			prev[r * env.getCols() + c] = row * env.getCols() + col;

		}

	}

//	private int numBlockAtTile(Position p) {
//		if (env.getBlock(p.getRow(), p.getCol()) != null) {// only 1 block
//			return 1;
//		} else if (env.checkTopBlock(p.getRow(), p.getCol()) != null) {
//			return 2;
//		}
//		return 0;
//	}

	static public String randomStatement(List<String> statements) {
		return statements.get((int) (Math.random() * statements.size()));
	}

	static boolean unstack = false;

	private static void STRIPS(LinkedList<Predicate> state, LinkedList<Predicate> goals, LinkedList<Rule> plan) {
		Stack<Object> goalStack = new Stack<>();
		// swap
		if (goals.size() > 1) {
			On firstGoal = (On) goals.get(0);
			On secondGoal = (On) goals.get(1);
			if (firstGoal.top.equals(secondGoal.bottom)) {
				Predicate first = goals.remove();
				goals.add(first);
			}
		}

		for (Predicate p : goals) {
			goalStack.push(p);
		}
		while (!goalStack.isEmpty()) {
			Object p = goalStack.pop();
			if ((p instanceof Predicate) && (myContains(state, ((Predicate) p)) != -1)) {
				continue;
			}
			if (p instanceof Rule) {
				updateStateAndPlan((Rule) p, state, plan, goals, goalStack);
				continue;
			}
			if (p instanceof Predicate) {
				chooseAction((Predicate) p, state, goalStack);
				continue;
			}
		}
	}

	private static void chooseAction(Predicate p, LinkedList<Predicate> state, Stack<Object> goalStack) {
		// TODO Auto-generated method stub
		if (p instanceof On) {
			String bottom = "";
			On o = (On) p;
			goalStack.push(new StackIt(o.top, o.bottom));
			goalStack.push(new Clear(o.bottom));
			goalStack.push(new Holding(o.top));
			if (myContains(state, new OnTable(o.bottom)) == -1 && goalStack.size() >= 4 && !unstack) {
				for (Predicate s : state) {
					if (s instanceof On && ((On) s).top.equals(o.bottom)) {
						bottom = ((On) s).bottom;
						break;
					}
				}
				goalStack.push(new PutDown(o.bottom));
				goalStack.push(new Holding(o.bottom));
				goalStack.push(new UnStackIt(o.bottom, bottom));
				goalStack.push(new On(o.bottom, bottom));
				goalStack.push(new Clear(o.bottom));
				goalStack.push(new Handempty());
				unstack = true;

			}

		} else if (p instanceof OnTable) {
			OnTable ot = (OnTable) p;
			goalStack.push(new PutDown(ot.block));
			goalStack.push(new Holding(ot.block));

		} else if (p instanceof Clear) {
			Clear c = (Clear) p;
			String top = "";
			for (Predicate p1 : state) {
				if (p1 instanceof On) {
					On o1 = (On) p1;
					if (o1.bottom.equals(c.block)) {
						top = o1.top;
//						break;
					}
				}
			}

			goalStack.push(new UnStackIt(top, c.block));
			goalStack.push(new On(top, c.block));
			goalStack.push(new Clear(top));
			goalStack.push(new Handempty());
		} else if (p instanceof Handempty) {
			Handempty he = (Handempty) p;
			String block = "";
			for (Predicate p1 : state) {
				if (p1 instanceof Holding) {
					Holding h1 = (Holding) p1;
					block = h1.block;
//					break;
				}
			}
			goalStack.push(new PutDown(block));
			goalStack.push(new Holding(block));

		} else if (p instanceof Holding) {
			Holding h = (Holding) p;
			if (myContains(state, new OnTable(h.block)) == 0) { // ontable
				goalStack.push(new PickUp(h.block));
				goalStack.push(new OnTable(h.block));
				goalStack.push(new Handempty());
				goalStack.push(new Clear(h.block));

			} else {
				String bottom = "";
				for (Predicate p1 : state) {
					if (p1 instanceof On) {
						On o1 = (On) p1;
						if (o1.top.equals(h.block)) {
							bottom = o1.bottom;
//							break;
						}
					}
				}
				goalStack.push(new UnStackIt(h.block, bottom));
				goalStack.push(new On(h.block, bottom));
				goalStack.push(new Clear(h.block));
				goalStack.push(new Handempty());

			}

		}

	}

	private static void updateStateAndPlan(Rule p, LinkedList<Predicate> state, LinkedList<Rule> plan, // change to
																										// state and
																										// plan linked
																										// list
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		// TODO Auto-generated method stub
		if (p instanceof StackIt) {
			StackIt si = (StackIt) p;
			remove(state, new Holding(si.block));
			remove(state, new Clear(si.target));
			state.push(new On(si.block, si.target));
			state.push(new Handempty());
			state.push(new Clear(si.block));
		} else if (p instanceof UnStackIt) {
			UnStackIt ui = (UnStackIt) p;
			remove(state, new Handempty());
			remove(state, new Clear(ui.block));
			remove(state, new On(ui.block, ui.target));
			state.push(new Holding(ui.block));
			state.push(new Clear(ui.target));
		} else if (p instanceof PickUp) {
			PickUp pu = (PickUp) p;
			remove(state, new Handempty());
			remove(state, new Clear(pu.block));
			remove(state, new OnTable(pu.block));
			state.push(new Holding(pu.block));
		} else if (p instanceof PutDown) {
			PutDown pd = (PutDown) p;
			remove(state, new Holding(pd.block));
			state.push(new Handempty());
			state.push(new Clear(pd.block));
			state.push(new OnTable(pd.block));

		}
		plan.add(p);

	}

	private static int myContains(LinkedList<Predicate> state, Predicate predicate) {
		// TODO Auto-generated method stub
		for (Predicate p : state) {
			if (predicate instanceof On && p instanceof On) {
				On pre = (On) predicate;
				On p1 = (On) p;
				if (pre.top.equals(p1.top) && pre.bottom.equals(p1.bottom)) {
					return 0;
				}

			} else if (predicate instanceof OnTable && p instanceof OnTable) {
				OnTable ot = (OnTable) p;
				OnTable pre = (OnTable) predicate;
				if (ot.block.equals(pre.block)) {
					return 0;
				}
			} else if (predicate instanceof Clear && p instanceof Clear) {
				Clear c = (Clear) p;
				Clear pre = (Clear) predicate;
				if (c.block.equals(pre.block)) {
					return 0;
				}
			} else if (predicate instanceof Handempty && p instanceof Handempty) {
				return 0;
			} else if (predicate instanceof Holding && p instanceof Holding) {
				Holding h = (Holding) p;
				Holding pre = (Holding) predicate;
				if (h.block.equals(pre.block)) {
					return 0;
				}

			}
		}
		return -1;
	}

	private static void remove(LinkedList<Predicate> state, Predicate predicate) {
		for (Predicate p : state) {
			if (predicate instanceof On && p instanceof On) {
				On pre = (On) predicate;
				On p1 = (On) p;
				if (pre.top.equals(p1.top) && pre.bottom.equals(p1.bottom)) {
					state.remove(p1);
					break;
				}
			} else if (predicate instanceof OnTable && p instanceof OnTable) {
				OnTable ot = (OnTable) p;
				OnTable pre = (OnTable) predicate;
				if (ot.block.equals(pre.block)) {
					state.remove(ot);
					break;
				}
			} else if (predicate instanceof Clear && p instanceof Clear) {
				Clear c = (Clear) p;
				Clear pre = (Clear) predicate;
				if (c.block.equals(pre.block)) {
					state.remove(c);
					break;
				}
			} else if (predicate instanceof Handempty && p instanceof Handempty) {
				state.remove(p);
				break;
			} else if (predicate instanceof Holding && p instanceof Holding) {
				Holding h = (Holding) p;
				Holding pre = (Holding) predicate;
				if (h.block.equals(pre.block)) {
					state.remove(h);
					break;
				}

			}
		}
	}

}

