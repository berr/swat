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
   <- !atualiza;
      !encontra(ouro);
      !sai_caverna.

	  
+!atualiza
   <- !atualiza(brisa);
   	  !atualiza(fedor);
	  !atualiza(brilho).
   
	  
+!encontra(ouro) : tem_ouro.
+!encontra(ouro)
   <- !explora;
      !atualiza;
      !encontra(ouro).

+!explora : posicao(X,Y) & tem_brilho(X,Y)
   <- +tem_ouro;
      +visitado_na_volta(X,Y).
	  
+!explora : posicao(X,Y) & adjacente(X,Y,A,B) & seguro(A,B) & not visitado(A,B) & dentro_mundo(A,B)
   <- move(A,B);
      -+posicao(A,B).
	  
+!explora : posicao(X,Y) & adjacente(X,Y,A,B) & visitado(A,B) & not visitado(A,B,2)
   <- move(A,B);
      +visitado(A,B,2);
      -+posicao(A,B).
	  
+!explora : posicao(X,Y) & adjacente(X,Y,A,B) & visitado(A,B) & not visitado(A,B,3)
   <- move(A,B);
      +visitado(A,B,3);
      -+posicao(A,B).
	  
+!explora : posicao(X,Y) & adjacente(X,Y,A,B) & seguro(A,B) & dentro_mundo(A,B)
   <- move(A,B);
      -+posicao(A,B).
	  
+!explora
   <- .print("nenhum lugar seguro").
   
+!sai_caverna : posicao(0,0)
   <- .print("sai!").
   
+!sai_caverna : posicao(X,Y) & adjacente(X,Y,A,B) & visitado(A,B) & not visitado_na_volta(A,B)
   <- move(A,B);
	  +visitado_na_volta(A,B);
      -+posicao(A,B);
	  !sai_caverna.
	  
+!sai_caverna : posicao(X,Y) & adjacente(X,Y,A,B) & visitado_na_volta(A,B) & not visitado_na_volta(A,B,2)
   <- move(A,B);
	  +visitado_na_volta(A,B,2);
      -+posicao(A,B);
	  !sai_caverna.

+!sai_caverna : posicao(X,Y) & adjacente(X,Y,A,B) & visitado_na_volta(A,B,2) & not visitado_na_volta(A,B,3)
   <- move(A,B);
	  +visitado_na_volta(A,B,3);
      -+posicao(A,B);
	  !sai_caverna.
+!avalia_periculosidade(X,Y) : true
   <- for(adjacente(X,Y,A,B)) {
   		if(tem_fedor(A,B)) {
			-periculosidade(X,Y,N);
			+periculosidade(X,Y,N+1);
			if(N+1 = 4) {
				+quero_matar;
				.print("vou_matar");
			}
		}
	  }.
	  
// por enquanto assume-se que o agente já se esteja na linha ou coluna do Wumpus 
+!vou_matar : posicao(X,Y) & periculosidade(A,B,4) & X = A & Y < B
   <- -+orientacao(sul);
	  atira(sul).
+!vou_matar : posicao(X,Y) & periculosidade(A,B,4) & X = A & Y > B
   <- -+orientacao(norte);
	  atira(norte).
+!vou_matar : posicao(X,Y) & periculosidade(A,B,4) & Y = B & X < A
   <- -+orientacao(leste);
	  atira(leste).
+!vou_matar : posicao(X,Y) & periculosidade(A,B,4) & Y = B & X > A
   <- -+orientacao(oeste);
	  atira(oeste).
   
+!atualiza(brisa) : posicao(X,Y) & brisa(X,Y)
   <- +tem_brisa(X,Y).
   
+!atualiza(brisa) : posicao(X,Y) & not brisa(X,Y)
   <- +~tem_brisa(X,Y).
   
+!atualiza(fedor) : posicao(X,Y) & fedor(X,Y)
   <- +tem_fedor(X,Y);
      for(adjacente(X,Y,A,B)) {
	  	+periculosidade(A,B,0);
		!avalia_periculosidade(A,B);
	  }.
   
+!atualiza(fedor) : posicao(X,Y) & not fedor(X,Y)
   <- +~tem_fedor(X,Y).
   
+!atualiza(brilho) : posicao(X,Y) & brilho(X,Y)
   <- +tem_brilho(X,Y).
   
+!atualiza(brilho) : posicao(X,Y) & not brilho(X,Y)
   <- +~tem_brilho(X,Y).   
   
+!atualiza(grito) : grito_cabreiro
   <- +wumpus_morreu;
      .print("MATEI!!!!!!!!!!!!!!!!").
	  
+!atualiza(grito) : true.


