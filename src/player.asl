// mars robot 1

/* Crencas iniciais */

posicao(0,0).
orientacao(norte).
bordas(0,0,6,6).

/* Regras de inferencia */
adjacente(X,Y,X+1,Y).
adjacente(X,Y,X-1,Y).
adjacente(X,Y,X,Y+1).
adjacente(X,Y,X,Y-1).

dentro_mundo(X,Y) :-
	bordas(EsquerdoInferiorX, EsquerdoInferiorY, DireitoSuperiorX, DireitoSuperiorY) & 
		(X >= EsquerdoInferiorX & Y >= EsquerdoInferiorY & X <= DireitoSuperiorX & Y <= DireitoSuperiorY).	
		
~tem_buraco(X,Y) :- ~tem_brisa(A,B) & adjacente(A,B, X,Y) & dentro_mundo(X,Y). 
~tem_wumpus(X,Y) :- ~tem_fedor(A,B) & adjacente(A,B, X,Y) & dentro_mundo(X,Y).

possivel_buraco(X,Y) :- posicao(X,Y) & tem_brisa(X,Y) & adjacente(X,Y,A,B) & dentro_mundo(A,B).
possivel_wumpus(X,Y) :- posicao(X,Y) & tem_fedor(X,Y) & adjacente(X,Y,A,B) & dentro_mundo(A,B).
possivel_ouro(X,Y)   :- posicao(X,Y) & tem_brilho(X,Y) & adjacente(X,Y,A,B) & dentro_mundo(A,B).

visitado(X,Y) :- tem_brisa(X,Y) | ~tem_brisa(X,Y).

seguro(X,Y) :- ~tem_buraco(X,Y) & ~tem_wumpus(X,Y).
seguro(X,Y) :- visitado(X,Y).
~seguro(X,Y) :- tem_buraco(X,Y) | tem_wumpus(X,Y).

/* Objetivo inicial */

!main. 

/* Planos */

+!main
   <- 
      !on_the_goods.

+!on_the_goods
   <- true.
	  

