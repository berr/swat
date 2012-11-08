import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class WumpusWorldEnv extends Environment {

    public static final int GRID_SIZE = 7;
    
	public static final int BLUE_FLAG  = 16;
	public static final int REG_FLAG = 32;
	public static final int OBSTACLE = 64;
	public static final int PIT = 128;	   

    static Logger logger = Logger.getLogger(SWATEnv.class.getName());

    private SWATModel model;
    private SWATView  view;
    
    @Override
    public void init(String[] args) {
        model = new SWATModel(10);
        view  = new SWATView(model);
        model.setView(view);
        updatePercepts();
    }
    
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
		String action_name = action.getFunctor(); 
        try {
            if (action_name.equals("move")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.move(x,y);
            } else if (action_name.equals("atira(norte)")){
				Location posicaoAgente = model.getAgPos(0);
				int x_agente = posicaoAgente.x;
				int y_agente = posicaoAgente.y;
				for (int outro_y = y_agente-1; outro_y>=0; outro_y--)
					mataSeEstiverEm(x_agente, outro_y);
			} else if (action_name.equals("atira(sul)")){
				Location posicaoAgente = model.getAgPos(0);
				int x_agente = posicaoAgente.x;
				int y_agente = posicaoAgente.y;
				for (int outro_y = y_agente+1; outro_y<GRID_SIZE; outro_y++)
					mataSeEstiverEm(x_agente, outro_y);
			} else if (action_name.equals("atira(leste)")){
				Location posicaoAgente = model.getAgPos(0);
				int x_agente = posicaoAgente.x;
				int y_agente = posicaoAgente.y;
				for (int outro_x = x_agente+1; outro_x<GRID_SIZE; outro_x++)
					mataSeEstiverEm(outro_x,y_agente);
			} else if (action_name.equals("atira(oeste)")){
				Location posicaoAgente = model.getAgPos(0);
				int x_agente = posicaoAgente.x;
				int y_agente = posicaoAgente.y;
				for (int outro_x = x_agente-1; outro_x>=0; outro_x--)
					mataSeEstiverEm(outro_x,y_agente);
			} else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updatePercepts();

        try {
            Thread.sleep(100);
        } catch (Exception e) {}
        return true;
    }
    
	void mataSeEstiverEm(int x, int y) {
		if (model.hasObject(WUMPUS, x, y)) {
			Literal percepcao = Literal.parseLiteral("grito_cabreiro");
			addPercept(percepcao);
			// TODO: tirar o bicho do mapa
		}
	}
	
	void criaPercepcoes(int x, int y) {
		Location posicaoAgente = model.getAgPos(0);
		int x_agente = posicaoAgente.x;
		int y_agente = posicaoAgente.y;
		Location posicao = new Location(x, y);
		
		if (model.hasObject(WUMPUS, posicao)){
			Literal percepcao = Literal.parseLiteral("fedor(" + x_agente + "," + y_agente + ")");
			addPercept(percepcao);
		}
		if (model.hasObject(PIT, posicao)){
			Literal percepcao = Literal.parseLiteral("brisa(" + x_agente + "," + y_agente + ")");
			addPercept(percepcao);
		}
	}

    void updatePercepts() {
        clearPercepts();
        
		// Atualiza as percepcoes do agente. Verificar os arredores dele pra ver
		// se tem poco ou Wumpus, criando brisa e/ou fedor. Verifica também a 
		// posicao atual em busca de brilho
		
		Location posicao = model.getAgPos(0);
		int x_agente = posicao.x;
		int y_agente = posicao.y;
		
		int x_atual, y_atual;
		
		if (x_agente - 1 >= 0) {
			criaPercepcoes(x_agente - 1, y_agente);
		}
		
		if (x_agente + 1 <= GRID_SIZE) {
			criaPercepcoes(x_agente + 1, y_agente);
		}
		
		if (y_agente - 1 >= 0) {
			criaPercepcoes(x_agente, y_agente - 1);
		}
		
		if (y_agente + 1 <= GRID_SIZE) {
			criaPercepcoes(x_agente, y_agente + 1);
		}
		
		if (model.hasObject(GOLD, posicao)){
			Literal percepcao = Literal.parseLiteral("brilho(" + x_agente + "," + y_agente + ")");
			addPercept(percepcao);
		}
		 								        
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
			for(int i = 0; i < GRID_SIZE && ag < agents_per_team; ++i){
				for(int j = 0; j < GRID_SIZE && ag < agents_per_team; ++j) {
					if (isCellAvailable(i, j)){
						setAgPos(ag, i ,j);
						ag++;
					}
				}
			}
			
			for(int i = 0; i < GRID_SIZE && ag < this.numberOfAgents; ++i){
				for(int j = GRID_SIZE; j >= 0 && ag < this.numberOfAgents; --j) {
					if (isCellAvailable(i, j)){
						setAgPos(ag, i ,j);
						ag++;
					}
				}
			}

		}
        
		
		boolean isCellAvailable(int x, int y) {
			return !hasObject(OBSTACLE, x, y) && !hasObject(PIT, x, y); 	
		}
		
        void move(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
			r1.x = x;
			r1.y = y;
            setAgPos(0, r1);
        }				
        
        
    }
    
    class SWATView extends GridWorldView {

        public SWATView(SWATModel model) {
            super(model, "Wumpus World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
            setVisible(true);
            repaint();
        }

        
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            String descricao = getDescricao(object);
			
			super.drawString(g, x, y, defaultFont, descricao);
        }
		
		// Monta uma string que diz o que tem naquela celula
		private String getDescricao(int object) {
			String resultado = "";
			
			if ((object & model.AGENT) != 0)
				resultado += " A";
			if ((object & WUMPUS) != 0)
				resultado += " W";
			if ((object & GOLD) != 0)
				resultado += " G";
			if ((object & PIT) != 0)
				resultado += " P";
			if ((object & GLITTER) != 0)
				resultado += " *";
			if ((object & BREEZE) != 0)
				resultado += " ~";
			if ((object & STENCH) != 0)
				resultado += " #";
			
			return resultado;			
		}        

    }    
}

