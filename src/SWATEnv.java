import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class SWATEnv extends Environment {

	public static final int GRID_SIZE = 16;

	public static final int BLUE_FLAG = 16;
	public static final int RED_FLAG = 32;
	public static final int WALL = 64;
	public static final int PIT = 128;

	static Logger logger = Logger.getLogger(SWATEnv.class.getName());

	public static final int NUMBER_OF_AGENTS = 0x10;

	private SWATModel model;
	private SWATView view;
	private List<String> agentNames;

	@Override
	public void init(String[] args) {
		model = new SWATModel(NUMBER_OF_AGENTS);
		view = new SWATView(model);
		model.setView(view);
		updatePercepts();

		try {
			jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(
					new FileInputStream(args[0]));
			MAS2JProject project = parser.mas();

			this.agentNames = new ArrayList<String>();

			for (AgentParameters ap : project.getAgents()) {
				String agName = ap.name;
				for (int cAg = 0; cAg < ap.qty; cAg++) {
					String numberedAg = agName;
					if (ap.qty > 1) {
						numberedAg += (cAg + 1);
					}
					this.agentNames.add(numberedAg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean executeAction(String ag, Structure action) {
		logger.info(ag + " doing: " + action);
		String action_name = action.getFunctor();

		int agentNumber = numberOfAgent(ag);

		try {
			if (action_name.equals("move")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.move(agentNumber, x, y);
			} else if (action_name.equals("move_towards_flag")) {
				String team = action.getTerm(0).toString();
				if (team.equals("red_team")) {
					model.moveTowardsRedFlag(agentNumber);
				} else {
					model.moveTowardsBlueFlag(agentNumber);
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		view.repaint();
		updatePercepts();

		try {
			Thread.sleep(100);
		} catch (Exception e) {
			// pass
		}

		return true;
	}

	private int numberOfAgent(String ag) {
		int i = 0;
		for (String name : this.agentNames) {
			if (name.equals(ag))
				return i;
			++i;
		}
		return 0xB00B5;
	}

	public String agentName(int agent) {
		return this.agentNames.get(agent);
	}

	void updatePercepts() {
		// clearPercepts();
	}

	class SWATModel extends GridWorldModel {

		private int numberOfAgents;
		private Location blueFlag;
		private Location redFlag;

		private SWATModel(int numberOfAgents) {
			super(GRID_SIZE, GRID_SIZE, numberOfAgents);

			this.numberOfAgents = numberOfAgents;

			initWorld();
		}

		private void moveTowardsLocation(int agentNumber, Location l) {
			Location agentLocation = getAgPos(agentNumber);
			
			int best_x;
			int best_y;
			
			if (l.x > agentLocation.x)
				best_x = agentLocation.x + 1;
			else if (l.x < agentLocation.x)
				best_x = agentLocation.x - 1;
			else
				best_x = agentLocation.x;
			
			if (l.y > agentLocation.y)
				best_y = agentLocation.y + 1;
			else if (l.y < agentLocation.y)
				best_y = agentLocation.y - 1;
			else
				best_y = agentLocation.y;
			
			if (isCellAvailableForAgent(best_x, best_y)){
				move(agentNumber, best_x, best_y);
				System.out.println(agentNumber + " to the best!");
				return;
			}
			
				
			for (int i = -1; i <= 1; i++){
				if (isCellAvailableForAgent(best_x, agentLocation.y + i)){
					move(agentNumber, best_x, agentLocation.y + i);
					System.out.println(agentNumber + " almost!");
					return;
				}
				
				if (isCellAvailableForAgent(agentLocation.x + i, best_y)){
					move(agentNumber, agentLocation.x + i, best_y);
					System.out.println(agentNumber + " almost!");
					return;
				}
					
			}
			
			while(true){
				int x_offset = Math.abs(random.nextInt() % 3) - 1;
				int y_offset = Math.abs(random.nextInt() % 3) - 1;
				int current_x = agentLocation.x + x_offset;
				int current_y = agentLocation.y + y_offset;
				if(isCellAvailableForAgent(current_x, current_y)){
					move(agentNumber, current_x, current_y);
					System.out.println(agentNumber + " random =(");
					return;
				}
			} 
		}

		public void moveTowardsRedFlag(int agentNumber) {
			moveTowardsLocation(agentNumber, redFlag);
		}

		public void moveTowardsBlueFlag(int agentNumber) {
			moveTowardsLocation(agentNumber, blueFlag);
		}

		public synchronized void move(int agent, int x, int y) {
			if (isCellAvailableForAgent(x, y))
				setAgPos(agent, new Location(x, y));
		}

		public static final double max_percentage = 0.27;
		public static final double min_percentage = 0.03;

		void initWorld() {

			setupFlags();
			setupObstacles();
			setupTeams();

			updateInitialPercepts();
		}

		private void setupFlags() {
			// Setup flags
			blueFlag = new Location(0, GRID_SIZE / 2);
			set(BLUE_FLAG, blueFlag.x, blueFlag.y);
			redFlag = new Location(GRID_SIZE - 1, GRID_SIZE / 2);
			set(RED_FLAG, redFlag.x, redFlag.y);
		}

		private void setupObstacles() {
			// Setup obstacles
			int max_quantity = (int) ((GRID_SIZE * GRID_SIZE) * (max_percentage));
			int min_quantity = (int) ((GRID_SIZE * GRID_SIZE) * (min_percentage));

			Random random_generator = new Random();
			int obstacle_quantity = random_generator.nextInt(max_quantity
					- min_quantity + 1)
					+ min_quantity;

			int placed_obstacles = 0;
			int x, y;
			while (placed_obstacles < obstacle_quantity) {
				do {
					x = random_generator.nextInt(GRID_SIZE);
					y = random_generator.nextInt(GRID_SIZE);
				} while (!isCellAvailable(x, y));

				int chosen = random_generator.nextInt(2);
				int terrain_type;
				if (chosen == 0)
					terrain_type = PIT;
				else
					terrain_type = WALL;

				add(terrain_type, x, y);
				++placed_obstacles;
			}
		}

		private void setupTeams() {
			// Setup teams
			int ag = 0;
			int agents_per_team = this.numberOfAgents / 2;
			// blue team agents
			for (int i = 0; i < GRID_SIZE && ag < agents_per_team; ++i) {
				for (int j = 0; j < GRID_SIZE && ag < agents_per_team; ++j) {
					if (isCellAvailable(i, j)) {
						setAgPos(ag, i, j);
						ag++;
					}
				}
			}

			// red team agents
			for (int i = GRID_SIZE - 1; i >= 0 && ag < this.numberOfAgents; --i) {
				for (int j = 0; j < GRID_SIZE && ag < this.numberOfAgents; ++j) {
					if (isCellAvailable(i, j)) {
						setAgPos(ag, i, j);
						ag++;
					}
				}
			}
		}

		private boolean isRedTeam(int agentNumber) {
			return !isBlueTeam(agentNumber);
		}

		private boolean isBlueTeam(int agentNumber) {
			return agentNumber < (this.numberOfAgents / 2);
		}

		private void updateInitialPercepts() {
			Literal borderLiteral = Literal.parseLiteral("borders(0,0,"
					+ (this.width - 1) + "," + (this.height - 1) + ").");

			addPercept(borderLiteral);

			Literal blueFlagLiteral = Literal.parseLiteral("red_flag(" + 0
					+ ", " + GRID_SIZE / 2 + ").");
			Literal redFlagLiteral = Literal.parseLiteral("red_flag("
					+ (GRID_SIZE - 1) + ", " + GRID_SIZE / 2 + ").");

			addPercept(blueFlagLiteral);
			addPercept(redFlagLiteral);

			for (int i = 0; i < this.numberOfAgents; ++i) {
				String agentName = "player" + i;

				Location location = getAgPos(i);
				Literal locationLiteral = Literal.parseLiteral("position("
						+ location.x + "," + location.y + ").");

				addPercept(agentName, locationLiteral);
				Literal teamLiteral;
				if (isRedTeam(i)) {
					teamLiteral = Literal.parseLiteral("team(red_team).");
				} else {
					teamLiteral = Literal.parseLiteral("team(blue_team).");
				}
				addPercept(agentName, teamLiteral);
			}

		}

		boolean isCellAvailableForAgent(int x, int y) {
			return !hasObject(WALL, x, y) && !hasObject(PIT, x, y)
					&& !hasObject(AGENT, x, y) 
					&& x > 0 && x < GRID_SIZE
					&& y > 0 && y < GRID_SIZE;
		}

		boolean isCellAvailable(int x, int y) {
			return !hasObject(WALL, x, y) && !hasObject(PIT, x, y)
					&& !hasObject(AGENT, x, y) && !hasObject(BLUE_FLAG, x, y)
					&& !hasObject(RED_FLAG, x, y);
		}

		int getObject(int x, int y) {
			return data[x][y];
		}

	}

	class SWATView extends GridWorldView {

		public SWATView(SWATModel model) {
			super(model, "SWAT World", 600);
			defaultFont = new Font("Arial", Font.BOLD, 16); // change default
			// font
			setVisible(true);
			repaint();
		}

		@Override
		public void draw(Graphics g, int x, int y, int object) {
			String descricao = getCellDescription(object, x, y);

			Color old_color = g.getColor();

			if ((object & RED_FLAG) != 0) {
				g.setColor(Color.RED);
				g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1,
						cellSizeH - 1);
			} else if ((object & BLUE_FLAG) != 0) {
				g.setColor(Color.BLUE);
				g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1,
						cellSizeH - 1);
			}

			g.setColor(old_color);

			if ((object & model.AGENT) != 0) {
				Color new_color;

				if (model.getAgAtPos(x, y) < model.getNbOfAgs() / 2)
					new_color = Color.BLUE;
				else
					new_color = Color.RED;

				g.setColor(new_color);
				g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4,
						cellSizeH - 4);
				g.setColor(old_color);
			}

			super.drawString(g, x, y, defaultFont, descricao);
		}

		@Override
		public void drawAgent(Graphics g, int x, int y, Color c, int id) {
			draw(g, x, y, ((SWATModel) model).getObject(x, y));
		}

		private String getAgentDescription(int x, int y) {
			int agentId = model.getAgAtPos(x, y);

			return "" + agentId;

		}

		private String getCellDescription(int object, int x, int y) {
			String result = "";

			if ((object & model.AGENT) != 0)
				result += getAgentDescription(x, y);
			if ((object & PIT) != 0)
				result += " U";
			if ((object & WALL) != 0)
				result += " |";
			if ((object & RED_FLAG) != 0)
				result += " RF";
			if ((object & BLUE_FLAG) != 0)
				result += " BF";

			return result;
		}

	}

}
