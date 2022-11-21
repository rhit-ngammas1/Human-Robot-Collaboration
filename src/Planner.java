import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Stack;

public class Planner {
	
	public static void main(String[] args) {
		LinkedList<Predicate> startState = new LinkedList<>();
		LinkedList<Predicate> goals = new LinkedList<>();
//		//Problem 1:
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new OnTable("A"));
		
//		//Problem 2:
//		startState.add(new OnTable("B"));
//		startState.add(new On("A", "B"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
		
//		//Problem 3:
//		startState.add(new OnTable("B"));
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("A"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
		
//		//Problem 4:
//		startState.add(new OnTable("A"));
//		startState.add(new On("B", "A"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
		
//		//Problem 5:
//		startState.add(new OnTable("A"));
//		startState.add(new OnTable("B"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Clear("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("B", "C"));
//		goals.add(new On("A", "B"));
		
//		//Problem 6:
//		startState.add(new OnTable("A"));
//		startState.add(new OnTable("B"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Clear("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 7:
//		startState.add(new On("A", "B"));
//		startState.add(new On("B", "C"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 8:
//		startState.add(new OnTable("A"));
//		startState.add(new On("B", "C"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("B"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 9:
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "B"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 10:
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "B"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("B", "C"));
//		goals.add(new On("A", "B"));
		
//		//Problem 11:
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "A"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 12:
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "A"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("B", "C"));
//		goals.add(new On("A", "B"));
		
//		//Problem 13:
//		startState.add(new On("C","B"));
//		startState.add(new On("B","A"));
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("A","B"));
//		goals.add(new On("B","C"));
		
//		//Problem 14:
//		startState.add(new On("C","B"));
//		startState.add(new On("B","A"));
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("B","C"));
//		goals.add(new On("A","B"));

		
		LinkedList<Rule> plan = new LinkedList<>();
		try {
			STRIPS(startState, goals, plan);
		}
		catch(Exception e){
			System.out.println("Planner needs help.");
			System.out.println("Exception that cause the program to crashes: " +e);
			return;
		}
		for(Predicate g:goals) {
			if(myContains(startState, g)==-1) {
				System.out.println("Planner needs help.The goal conditions are not satisfied in the final state");
				return;
			}
		}
		System.out.println("Success! The goal conditions are satisfied in the final state.");
		if (plan.isEmpty()) {
			System.out.println("No plan.");
		}
		for(Rule a: plan) {
			System.out.println(a);
		}
		System.out.println("\nState:");
		for (Predicate p : startState) {
			System.out.println(p);
		}

		
	}
	static boolean unstack=false;
		//G = goal, S=initial state
	private static void STRIPS(LinkedList<Predicate> state, LinkedList<Predicate> goals, LinkedList<Rule> plan){
		Stack<Object> goalStack = new Stack<>();
		//swap
		if(goals.size()>1) {
			On firstGoal=(On)goals.get(0);
			On secondGoal=(On)goals.get(1);
			if(firstGoal.top.equals(secondGoal.bottom)) {
				Predicate first= goals.remove();
				goals.add(first);
			}
		}

		for (Predicate p : goals) {
			goalStack.push(p);
		}
		while(!goalStack.isEmpty()) { 
			Object p = goalStack.pop();
			if ((p instanceof Predicate) && (myContains(state, ((Predicate) p)) != -1)) { 
				continue; }
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
		if(p instanceof On) {
			String bottom="";
			On o=(On) p;
			goalStack.push(new StackIt(o.top,o.bottom));
			goalStack.push(new Clear(o.bottom));
			goalStack.push(new Holding(o.top));
			if(myContains(state, new OnTable(o.bottom))==-1 && goalStack.size()>=4 && !unstack) { 
				for(Predicate s:state) {
					if(s instanceof On && ((On)s).top.equals(o.bottom)) {
						bottom=((On)s).bottom;
						break;
					}
				}
				goalStack.push(new PutDown(o.bottom));
				goalStack.push(new Holding(o.bottom));
				goalStack.push(new UnStackIt(o.bottom, bottom));
				goalStack.push(new On(o.bottom,bottom));
				goalStack.push(new Clear(o.bottom));
				goalStack.push(new Handempty());
				unstack=true;
				
			}
			

		}
		else if(p instanceof OnTable) {
			OnTable ot=(OnTable) p;
			goalStack.push(new PutDown(ot.block));
			goalStack.push(new Holding(ot.block));
			
		}
		else if(p instanceof Clear) {
			Clear c=(Clear) p;
			String top="";
			for(Predicate p1:state) {
				if(p1 instanceof On) {
					On o1=(On) p1;
					if(o1.bottom.equals(c.block)) {
						top=o1.top;
//						break;
					}
				}
			}
			
			goalStack.push(new UnStackIt(top,c.block));
			goalStack.push(new On(top,c.block));
			goalStack.push(new Clear(top));
			goalStack.push(new Handempty());
		}
		else if(p instanceof Handempty) {
			Handempty he=(Handempty) p;
			String block="";
			for(Predicate p1:state) {
				if(p1 instanceof Holding) {
					Holding h1=(Holding) p1;
					block=h1.block;
//					break;
				}
			}
			goalStack.push(new PutDown(block));
			goalStack.push(new Holding(block));
			
		}
		else if(p instanceof Holding) {
			Holding h=(Holding) p;
			if(myContains(state, new OnTable(h.block))==0) { //ontable
				goalStack.push(new PickUp(h.block));
				goalStack.push(new OnTable(h.block));
				goalStack.push(new Handempty());
				goalStack.push(new Clear(h.block));

				
			}
			else {
				String bottom="";
				for(Predicate p1:state) {
					if(p1 instanceof On) {
						On o1=(On) p1;
						if(o1.top.equals(h.block)) {
							bottom=o1.bottom;
//							break;
						}
					}
				}
				goalStack.push(new UnStackIt(h.block,bottom));
				goalStack.push(new On(h.block,bottom));
				goalStack.push(new Clear(h.block));
				goalStack.push(new Handempty());
				
			}
			
		}
		
	}

	private static void updateStateAndPlan(Rule p, LinkedList<Predicate> state, LinkedList<Rule> plan, //change to state and plan linked list
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		// TODO Auto-generated method stub
		if(p instanceof StackIt) {
			StackIt si=(StackIt) p;
			remove(state,new Holding(si.block));
			remove(state,new Clear(si.target));
			state.push(new On(si.block,si.target));
			state.push(new Handempty());
			state.push(new Clear(si.block));
		}
		else if(p instanceof UnStackIt) {
			UnStackIt ui=(UnStackIt) p;
			remove(state,new Handempty());
			remove(state,new Clear(ui.block));
			remove(state,new On(ui.block,ui.target));
			state.push(new Holding(ui.block));
			state.push(new Clear(ui.target));
		}
		else if(p instanceof PickUp) {
			PickUp pu=(PickUp) p;
			remove(state,new Handempty());
			remove(state,new Clear(pu.block));
			remove(state,new OnTable(pu.block));
			state.push(new Holding(pu.block));
		}
		else if(p instanceof PutDown) {
			PutDown pd=(PutDown) p;
			remove(state,new Holding(pd.block));
			state.push(new Handempty());
			state.push(new Clear(pd.block));
			state.push(new OnTable(pd.block));
			
		}
		plan.add(p);
		
	}

	private static int myContains(LinkedList<Predicate> state, Predicate predicate) {
		// TODO Auto-generated method stub
		for (Predicate p : state) {
			if(predicate instanceof On && p instanceof On) {
				On pre=(On) predicate;
				On p1=(On) p;
				if(pre.top.equals(p1.top)&&pre.bottom.equals(p1.bottom)) {
					return 0;
				}

			}
			else if(predicate instanceof OnTable && p instanceof OnTable) {
				OnTable ot=(OnTable) p;
				OnTable pre=(OnTable) predicate;
				if(ot.block.equals(pre.block)) {
					return 0;
				}
			}
			else if(predicate instanceof Clear && p instanceof Clear) {
				Clear c=(Clear) p;
				Clear pre=(Clear) predicate;
				if(c.block.equals(pre.block)) {
					return 0;
				}
			}
			else if(predicate instanceof Handempty && p instanceof Handempty) {
				return 0;
			}
			else if(predicate instanceof Holding && p instanceof Holding) {
				Holding h=(Holding) p;
				Holding pre=(Holding) predicate;
				if(h.block.equals(pre.block)) {
					return 0;
				}
				
			}
		}
		return -1;
	}
	
	private static void remove(LinkedList<Predicate> state, Predicate predicate) {
		for (Predicate p : state) {
			if(predicate instanceof On && p instanceof On) {
				On pre=(On) predicate;
				On p1=(On) p;
				if(pre.top.equals(p1.top)&&pre.bottom.equals(p1.bottom)) {
					state.remove(p1);
					break;
				}
			}
			else if(predicate instanceof OnTable && p instanceof OnTable) {
				OnTable ot=(OnTable) p;
				OnTable pre=(OnTable) predicate;
				if(ot.block.equals(pre.block)) {
					state.remove(ot);
					break;
				}
			}
			else if(predicate instanceof Clear && p instanceof Clear) {
				Clear c=(Clear) p;
				Clear pre=(Clear) predicate;
				if(c.block.equals(pre.block)) {
					state.remove(c);
					break;
				}
			}
			else if(predicate instanceof Handempty && p instanceof Handempty) {
				state.remove(p);
				break;
			}
			else if(predicate instanceof Holding && p instanceof Holding) {
				Holding h=(Holding) p;
				Holding pre=(Holding) predicate;
				if(h.block.equals(pre.block)) {
					state.remove(h);
					break;
				}
				
			}
		}
	}

	
}
