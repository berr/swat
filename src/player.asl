adjacent(X,Y,X+1,Y).
adjacent(X,Y,X-1,Y).
adjacent(X,Y,X,Y+1).
adjacent(X,Y,X,Y-1).

//adjacent(X,Y,X+1,Y+1).
//adjacent(X,Y,X+1,Y-1).
//adjacent(X,Y,X-1,Y+1).
//adjacent(X,Y,X-1,Y-1).


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
      !on_the_goods.

+!on_the_goods : position(X, Y) & adjacent(X,Y,A,B) & inside_world(A,B)
   <- 
   	move(A,B).
	  

