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

ally(I,U) :- team(I,T) & team(U,T).


inside_world(X,Y) :-
	borders(LeftBottomX, LeftBottomY, RightTopX, RightTopY) & 
		(X >= LeftBottomX & Y >= LeftBottomY & X <= RightTopX & Y <= RightTopY).	
		

/* Objetivo inicial */

!main. 

/* Planos */

+!main
   <- 
      !attack;
	  !return.

	  
   
+!attack : number(I) & team(I,T) & enemy(T,E) & position(X,Y) & flag(E, X, Y)
   <-
   	grab_flag(E).

+!attack : number(I) & team(I,T) & enemy(T, E) & flag(E, X, Y)
   <- 
   	move_towards(X, Y);
	!attack.
	

+!return : number(I) & team(I,T) & enemy(T,E) & position(X,Y) & base(T, X, Y) & carrying_flag(I,E)
   <-
     win.
	 
+!return : number(I) & team(I,T) & base(T, A, B) 
   <-
     move_towards(A, B);
	 !return.

+seen(X) : number(I) & not ally(I,X)
   <-
    fire_the_mighty_weapon.
	
//+seen(X) : number(I) & ally(I,X)
//   <-
//    greet.
