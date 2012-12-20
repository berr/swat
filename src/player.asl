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
      !attack;
	  !return.

+!attack : team(T) & enemy(T, E) & flag(E, X, Y)
   <- 
   	move_towards(X, Y);
	!attack.
	  
//+!attack : team(T) & enemy(T,E) & position(X,Y) & flag(E, X, Y)
//   <-
//   	grab_flag.
	
+!return : team(T) & base(T, A, B)
   <-
     move_towards(A, B);
	 !return.
	 
+!return : team(T) & position(X,Y) & base(T, X, Y)
   <-
     win.
	
	
	
	
