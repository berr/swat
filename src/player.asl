adjacent(X,Y,X+1,Y).
adjacent(X,Y,X-1,Y).
adjacent(X,Y,X,Y+1).
adjacent(X,Y,X,Y-1).

adjacent(X,Y,X+1,Y+1).
adjacent(X,Y,X+1,Y-1).
adjacent(X,Y,X-1,Y+1).
adjacent(X,Y,X-1,Y-1).

enemy(blue_team, red_team).
enemy(red_team, blue_team).


inside_world(X,Y) :-
	borders(LeftBottomX, LeftBottomY, RightTopX, RightTopY) & 
		(X >= LeftBottomX & Y >= LeftBottomY & X <= RightTopX & Y <= RightTopY).	
		

seguro(X,Y) :- ~tem_buraco(X,Y) & ~tem_wumpus(X,Y).
seguro(X,Y) :- visitado(X,Y).
~seguro(X,Y) :- tem_buraco(X,Y) | tem_wumpus(X,Y).

/* Objetivo inicial */

!main. 

/* Planos */

+!main
   <- 
      !attack.

+!attack : team(T) & not has_flag(T) & enemy(T, E)
   <- 
   	move_towards_flag(E);
	!attack.
	  

