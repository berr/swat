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
import java.util.Random;
import java.util.logging.Logger;

public class SWATEnv extends Environment {

    public static final int GRID_SIZE = 16;
    
	public static final int BLUE_FLAG  = 16;
	public static final int RED_FLAG = 32;
	public static final int WALL = 64;
	public static final int PIT = 128;	   

    static Logger logger = Logger.getLogger(SWATEnv.class.getName());

    private SWATModel model;
    private SWATView  view;
    
    @Override
    public void init(String[] args) {
        model = new SWATModel(10); // fixed number of players on each team
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
		
		void initWorld() {
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
        
		
		boolean isCellAvailable(int x, int y) {
			return !hasObject(OBSTACLE, x, y) && !hasObject(PIT, x, y) && !hasObject(AGENT, x, y); 	
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
			            
        	System.out.println("drawing generic!");
            
			super.drawString(g, x, y, defaultFont, descricao);
        }
        
        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	draw(g, x, y, ((SWATModel)model).getObject(x,y));
        }
		
		private String getAgentDescription(int x, int y) {
			int agentId = model.getAgAtPos(x, y);
			
			
			if (agentId < model.getNbOfAgs() / 2)
				return agentId + "B";
			else
				return agentId + "R";
				
		}


		private String getCellDescription(int object, int x, int y) {
			String result = "";
			
			if ((object & model.AGENT) != 0)
				result += getAgentDescription(x, y);
			if ((object & PIT) != 0)
				result += " P";
			if ((object & WALL) != 0)
				result += " O";
			if ((object & RED_FLAG) != 0)
				result += " RF";
			if ((object & BLUE_FLAG) != 0)
				result += " BF";
			
			
			return result;			
		}
		

    }
    
}

