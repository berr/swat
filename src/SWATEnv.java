import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.peer.LightweightPeer;
import java.util.Random;
import java.util.logging.Logger;

public class SWATEnv extends Environment {

    public static final int GRID_SIZE = 16;
    
	public static final int BLUE_FLAG  = 16;
	public static final int RED_FLAG = 32;
	public static final int WALL = 64;
	public static final int PIT = 128;	   

    static Logger logger = Logger.getLogger(SWATEnv.class.getName());

    public static final int NUMBER_OF_AGENTS = 0x10;
    
    private SWATModel model;
    private SWATView  view;
    
    @Override
    public void init(String[] args) {
        model = new SWATModel(NUMBER_OF_AGENTS);
        view  = new SWATView(model);
        model.setView(view);
        updatePercepts();
    }
    
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
		String action_name = action.getFunctor(); 
//        try {
//            if (action_name.equals("move")) {
//                int x = (int)((NumberTerm)action.getTerm(0)).solve();
//                int y = (int)((NumberTerm)action.getTerm(1)).solve();
//                model.move(x,y);
//            } else if (action_name.equals("atira(norte)")){
//				Location posicaoAgente = model.getAgPos(0);
//				int x_agente = posicaoAgente.x;
//				int y_agente = posicaoAgente.y;
//				for (int outro_y = y_agente-1; outro_y>=0; outro_y--)
//					mataSeEstiverEm(x_agente, outro_y);
//			} else if (action_name.equals("atira(sul)")){
//				Location posicaoAgente = model.getAgPos(0);
//				int x_agente = posicaoAgente.x;
//				int y_agente = posicaoAgente.y;
//				for (int outro_y = y_agente+1; outro_y<GRID_SIZE; outro_y++)
//					mataSeEstiverEm(x_agente, outro_y);
//			} else if (action_name.equals("atira(leste)")){
//				Location posicaoAgente = model.getAgPos(0);
//				int x_agente = posicaoAgente.x;
//				int y_agente = posicaoAgente.y;
//				for (int outro_x = x_agente+1; outro_x<GRID_SIZE; outro_x++)
//					mataSeEstiverEm(outro_x,y_agente);
//			} else if (action_name.equals("atira(oeste)")){
//				Location posicaoAgente = model.getAgPos(0);
//				int x_agente = posicaoAgente.x;
//				int y_agente = posicaoAgente.y;
//				for (int outro_x = x_agente-1; outro_x>=0; outro_x--)
//					mataSeEstiverEm(outro_x,y_agente);
//			} else {
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        updatePercepts();
//
//        try {
//            Thread.sleep(100);
//        } catch (Exception e) {}
        return true;
    }
    	
	

    void updatePercepts() {
        clearPercepts();        
    }

    class SWATModel extends GridWorldModel {
        
		private int numberOfAgents;
		
        private SWATModel(int numberOfAgents) {
            super(GRID_SIZE, GRID_SIZE, numberOfAgents);            		            
            
			this.numberOfAgents = numberOfAgents;
			
			initWorld();            
        }
		
        public static final double max_percentage = 0.27;
        public static final double min_percentage = 0.03;
		void initWorld() {
			// Setup flags
			set(BLUE_FLAG, 0, GRID_SIZE/2);
			set(RED_FLAG, GRID_SIZE-1, GRID_SIZE/2);
			
			// Setup obstacles
			int max_quantity = (int)((GRID_SIZE*GRID_SIZE)*(max_percentage));
			int min_quantity = (int)((GRID_SIZE*GRID_SIZE)*(min_percentage));
			
			Random random_generator = new Random();
			int obstacle_quantity = random_generator.nextInt(max_quantity-min_quantity+1) + min_quantity;
			
			int placed_obstacles = 0;
			int x, y;
			while(placed_obstacles < obstacle_quantity){
				do {
					x = random_generator.nextInt(GRID_SIZE);
					y = random_generator.nextInt(GRID_SIZE);
				} while (!isCellAvailable(x,y));
				
				int chosen = random_generator.nextInt(2);
				int terrain_type;
				if (chosen == 0)
					terrain_type = PIT;
				else
					terrain_type = WALL;
				
				add(terrain_type, x, y);
				++placed_obstacles;
			}
			
			// Setup teams
			int ag = 0;
			int agents_per_team = this.numberOfAgents / 2;
			// blue team agents
			for(int i = 0; i < GRID_SIZE && ag < agents_per_team; ++i){
				for(int j = 0; j < GRID_SIZE && ag < agents_per_team; ++j) {
					if (isCellAvailable(i, j)){
						setAgPos(ag, i ,j);
						ag++;
					}
				}
			}
			
			// red team agents
			for(int i = GRID_SIZE - 1; i >= 0 && ag < this.numberOfAgents; --i){
				for(int j = 0; j < GRID_SIZE && ag < this.numberOfAgents; ++j) {
					if (isCellAvailable(i, j)){
						setAgPos(ag, i ,j);
						ag++;
					}
				}
			}
		}
        
		
		boolean isCellAvailableForAgent(int x, int y){
			return !hasObject(WALL, x, y) && !hasObject(PIT, x, y) && 
			   !hasObject(AGENT, x, y);
		}
		
		boolean isCellAvailable(int x, int y) {
			return !hasObject(WALL, x, y) && !hasObject(PIT, x, y) && 
				   !hasObject(AGENT, x, y) && 
				   !hasObject(BLUE_FLAG, x,y) && !hasObject(RED_FLAG, x,y); 	
		}
		
		
		int getObject(int x, int y){
			return data[x][y];
		}
        
    }
    
    class SWATView extends GridWorldView {

        public SWATView(SWATModel model) {
            super(model, "SWAT World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
            setVisible(true);
            repaint();
        }

        
        @Override
        public void draw(Graphics g, int x, int y, int object) {
        	String descricao = getCellDescription(object, x, y);
			     
        	Color old_color = g.getColor();
        	
        	if((object & RED_FLAG) != 0){
        		g.setColor(Color.RED);
        		g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        	} else if((object & BLUE_FLAG) != 0) {
        		g.setColor(Color.BLUE);
        		g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        	}
        	
        	
        	g.setColor(old_color);	
        	
        	if ((object & model.AGENT) != 0){
        		Color new_color;
        		
        		if (model.getAgAtPos(x,y) < model.getNbOfAgs() / 2)
        			new_color = Color.BLUE;
        		else
        			new_color = Color.RED;
        		
        		g.setColor(new_color);
        		g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        		g.setColor(old_color);
        	}
        	
			super.drawString(g, x, y, defaultFont, descricao);
        }
        
        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	draw(g, x, y, ((SWATModel)model).getObject(x,y));
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

