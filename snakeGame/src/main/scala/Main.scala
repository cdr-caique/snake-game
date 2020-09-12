/*=======================================
Nome: 					snakeGame
Disciplina: 		ECOM04 - Linguagens de Programação
Professor:			João Paulo Reus Rodrigues Leite
Data (código):	16/09/2018
Data (entrega):	20/09/2018
Propósito:			Exemplo de código na linguagem Scala, implementado o clássico jogo "snake", ou "jogo da cobrinha".
								O objetivo é resolver um problema real de produtividade e motivação em campo de trabalho,
								permitindo relaxamento do usuário enquanto toma um café.

Alunos:	Andrei Alves Cardoso
				Breno Salgado de Oliveira
				Caíque Cléber Dias de Rezende
				Erika Mayumi Saito Tagima
				Leonardo Said da Costa
				Tales Henrique Carvalho
				
*/

//Básico para usar o scalafx.
import scalafx.application.JFXApp

//Para pode usar utilidades de cenas.
//(Gráficos em geral, eventos de entrada e saída)
import scalafx.scene.Scene

//Tela e contexto gráfico, para poder usar gráficos rasterizados.
import scalafx.scene.canvas.Canvas
import scalafx.scene.canvas.GraphicsContext

//Para armazenar as posições da cobra
import scalafx.geometry.Point2D

//Classe que encapsula uma cobrinha.
//Uma "cobrinha" é composta por vários segmentos, cada um em uma posição x e y definidas.
class Cobrinha(val bordasTabuleiro:Point2D, val posicaoInicial:Point2D){
	//O comprimento máximo da cobrinha corresponde à área do tabuleiro.
	private var _comprimentoMaximo = (bordasTabuleiro.x*bordasTabuleiro.y).toInt;
	//A posição de cada segmento da cobra é colocada uma array.
	private var segmentos:Array[Point2D] = new Array[Point2D](_comprimentoMaximo);
	//Inicializa aleatoriamente a direção da cobrinha.
	//0 = direita, 1 = cima, 2 = esquerda, 3 = baixo.
	private var _direcao:Int = (new scala.util.Random()).nextInt(4);
	//Estado da cobrinha: 0 = viva, -1 = morta, 1 = venceu
	private var _estado:Int = 0;
	//Comprimento da cobrinha, em número de segmentos.
	private var _comprimento:Int = 1;
	//Inicializa a posição da cabeça.
	segmentos(0) = posicaoInicial;
	
	//Reconhecedores de estado.
	def comprimento = _comprimento;
	def estado = _estado;
	def estaViva = if(estado==0) true else false;
	def comprimentoMaximo = _comprimentoMaximo;
	
	//Retorna true se o ponto pertencer ao corpo da cobra.
	def Pertence(ponto:Point2D):Boolean={
		var t:Int = 0;
		for(t <- 0 to _comprimento-1){
			if(ponto==segmentos(t)){
				return true;
			}
		}
		return false;
	}
	
	//Retorna a próxima posição do segmento da cabeça.
	def ObterProximaPosicao():Point2D={
		if(estado!=0){
			//Verifica o estado: a cobra não se move se estiver morta.
			return segmentos(0);
		}
		_direcao match{
			case 0 =>	return new Point2D(segmentos(0).x+1,segmentos(0).y);	//Direita
			case 1 =>	return new Point2D(segmentos(0).x,segmentos(0).y-1);	//Cima
			case 2 =>	return new Point2D(segmentos(0).x-1,segmentos(0).y);	//Esquerda
			case 3 =>	return new Point2D(segmentos(0).x,segmentos(0).y+1);	//Baixo
			case _ => 	return segmentos(0);
		}
	}
	
	//Recebedor de direção.
	def direcao = _direcao;
	
	//Atribuidor de direção.
	//Ele só vai aceitar e a nova direção não vai colocar a cobra em perigo.
	//Isto é, se a cobra não vai cruzar consigo mesma ou sair do tabuleiro
	//Obs.: NÃO SEPARE O SUBTRAÇO DO IGUAL
	def direcao_= (novoValor: Int){
		//Obter próxima posição se obedecer a mudança.
		var tempDir = _direcao;
		_direcao = novoValor;
		var novoProximo = ObterProximaPosicao();
		_direcao = tempDir;
		
		if(novoProximo.x>=0 && novoProximo.x<bordasTabuleiro.x && novoProximo.y>=0 && novoProximo.y<bordasTabuleiro.y && Pertence(novoProximo)==false){
			//Está tudo bem.
			_direcao = novoValor;
		}
	}
	
	//Retorna a posição (x,y) de um segmento.
	def ObterPosicao(indiceSegmento:Int):Point2D={
		return segmentos(indiceSegmento);
	}
	
	//Faz a cobra crescer 1 unidade.
	def Alimenta():Unit={
		if(_comprimento<comprimentoMaximo){
			_comprimento+=1;
		}
		if(_comprimento==comprimentoMaximo){
			//Cobra venceu.
			_estado = 1;
		}
	}
	
	//Movimenta a cobra para frente em 1 unidade, se estiver viva.
	def Movimenta():Unit={
		//Verificar vivacidade.
		if(estado!=0){
			return;
		}
		
		var proximo = ObterProximaPosicao();
		if(proximo.x < 0 || proximo.x > bordasTabuleiro.x || proximo.y < 0 || proximo.y > bordasTabuleiro.y || Pertence(proximo)==true){
			//A cobra morreu.
			_estado = -1;
			return;
		}
		
		var t:Int = 0;
		for(t <- comprimentoMaximo-1 to 1 by -1){
			segmentos(t) = segmentos(t-1);
		}
		
		segmentos(t) = proximo;
	}
}

//Encapsula métodos úteis para lidar com eventos de janela e gráficos nesse jogo.
class Janela(val nome:String, val largura:Int, val altura:Int){
	//Criar tela (canvas) com atributos dados.
	private var cv = new Canvas(largura,altura);
	
	//Criar contexto grafico para desenhar.
	private var gc = cv.graphicsContext2D;
	
	//Variáveis para guardar o estado dos direcionais.
	private var _teclaDireita:Boolean 	= false;
	private var _teclaCima:Boolean 		= false;
	private var _teclaEsquerda:Boolean 	= false;
	private var _teclaBaixo:Boolean 	= false;
	private var _teclaPausa:Boolean 	= false;
	private var _teclaCorre:Boolean 	= false;
	
	//Variável para guardar a última direção mostrada pelos direcionais.
	private var _direcao:Int			= -1;
	
	//Criar cena.
	private val cena = new Scene(largura, altura){
		fill = scalafx.scene.paint.Color.rgb(0,0,0);
		content = cv
		
		//Evento para detectar pressão da tecla.
		onKeyPressed = (evento:javafx.scene.input.KeyEvent) => {
			val valorAttr = true;
			
			var codigoTecla = evento.getCode();
			if(codigoTecla==javafx.scene.input.KeyCode.RIGHT){
				_teclaDireita 	= valorAttr;
				_direcao = if(valorAttr==true) 0 else _direcao;
			}else if(codigoTecla==javafx.scene.input.KeyCode.UP){
				_teclaCima		= valorAttr;
				_direcao = if(valorAttr==true) 1 else _direcao;
			}else if(codigoTecla==javafx.scene.input.KeyCode.LEFT){
				_teclaEsquerda	= valorAttr;
				_direcao = if(valorAttr==true) 2 else _direcao;
			}else if(codigoTecla==javafx.scene.input.KeyCode.DOWN){
				_teclaBaixo		= valorAttr;
				_direcao = if(valorAttr==true) 3 else _direcao;
			}else if(codigoTecla==javafx.scene.input.KeyCode.ESCAPE){
				_teclaPausa		= valorAttr;
			}else if(codigoTecla==javafx.scene.input.KeyCode.SPACE){
				_teclaCorre		= valorAttr;
			}
		}
		
		onKeyReleased = (evento:javafx.scene.input.KeyEvent) => {
			val valorAttr = false;
			
			var codigoTecla = evento.getCode();
			if(codigoTecla==javafx.scene.input.KeyCode.RIGHT){
				_teclaDireita 	= valorAttr;
			}else if(codigoTecla==javafx.scene.input.KeyCode.UP){
				_teclaCima		= valorAttr;
			}else if(codigoTecla==javafx.scene.input.KeyCode.LEFT){
				_teclaEsquerda	= valorAttr;
			}else if(codigoTecla==javafx.scene.input.KeyCode.DOWN){
				_teclaBaixo		= valorAttr;
			}else if(codigoTecla==javafx.scene.input.KeyCode.ESCAPE){
				_teclaPausa		= valorAttr;
			}else if(codigoTecla==javafx.scene.input.KeyCode.SPACE){
				_teclaCorre		= valorAttr;
			}
		}
	}
	
	//Criar uma janela com título e cena definidas, e que não redimensiona.
	private val stage = new JFXApp.PrimaryStage{
		title = nome
		scene = cena
		resizable = false
	}
	
	//Reconhecedores de estado.
	def contextoGrafico = 	gc;
	def teclaDireita	=	_teclaDireita;
	def teclaCima	 	= 	_teclaCima;
	def teclaEsquerda 	= 	_teclaEsquerda;
	def teclaBaixo 		= 	_teclaBaixo;
	def teclaPausa 		= 	_teclaPausa;
	def teclaCorre 		= 	_teclaCorre;
	def direcao 		=	_direcao;
	
	//Desenha um retângulo com cor e tamanho definido.
	def DesenhaRetangulo(x:Double, y:Double, largura:Double, altura:Double,cor:scalafx.scene.paint.Color){
		gc.fill = cor;
		gc.fillRect(x,y,largura,altura);
	}
	
	//Desenha um quadrado decorado, com uma borda externa correspondente a 10% do lado.
	def DesenhaRetanguloDecorado(x:Double, y:Double, largura:Double, altura:Double,corExterna:scalafx.scene.paint.Color, corInterna:scalafx.scene.paint.Color):Unit={
		DesenhaRetangulo(x,y,largura,altura,corExterna);
		DesenhaRetangulo(x + largura*0.1,y  + altura*0.1,largura * 0.8,altura * 0.8,corInterna);
	}
	
	//Limpa a tela com uma cor definida.
	def LimpaTela(cor:scalafx.scene.paint.Color)={
		cena.fill = cor;
		gc.clearRect(0,0,largura,altura);
	}
	
	//Desenha uma grade que se extende por toda a área da tela.
	def DesenhaGrade(larguraGrade:Double, alturaGrade:Double, cor:scalafx.scene.paint.Color):Unit={
		
		var pos:Double = 0.0;
		gc.stroke = cor;
		
		while(pos<largura){
			gc.strokeLine(pos, 0.0, pos, altura);
			pos+=larguraGrade;
		}
		
		pos = 0.0;
		while(pos<altura){
			gc.strokeLine(0.0, pos, largura, pos);
			pos+=alturaGrade;
		}
	}
	
	//Limpa "buffer" de entrada.
	def LimpaDirecionais():Unit={
		_teclaDireita 	= false;
		_teclaCima 		= false;
		_teclaEsquerda 	= false;
		_teclaBaixo 	= false;
		_direcao 		= -1;
	}
	def LimpaEntradas():Unit={
		LimpaDirecionais();
		_teclaPausa	=	false;
		_teclaCorre	=	false;
	}
	
	//Desenha um texto centralizado.
	def DesenhaTextaoCentralizado(texto:String, x:Double, y:Double, cor:scalafx.scene.paint.Color, fonte:String = "Comic Sans", tamanhoFonte:Int = 150){
		gc.font = new scalafx.scene.text.Font(fonte, tamanhoFonte);
		gc.textAlign = scalafx.scene.text.TextAlignment.CENTER;
		gc.fill = cor;
		gc.fillText(texto,x,y+tamanhoFonte*0.3);
	}
}	

//Objeto principal.
object snakeGame {
	
	//===================================================================================
	//Uma variedade de configurações
	//===================================================================================
	
	//Tamanho da grade, em pixels.
	val tamanhoGrade = 70;
	//Largura do tabuleiro.
	val larguraTabuleiro:Int = 16;
	//Altura do tabuleiro
	val alturaTabuleiro:Int = 9;
	
	val baseIntervaloLento:Double = 250.0;
	var intervaloLento:Double = baseIntervaloLento;
	val proporcaoIntervaloRapido:Double = 0.25;
	//Cada vez que a cobra come algo, a velocidade dela aumenta.
	//A taxa é dado por esse valor: (quanto menor, mais difícil)
	//((0.995 costuma ser bom))
	val proporcaoComida:Double = 0.995;
	
	//Cor da cobra. Vermelhos aleatórios que achei por aí.
	//Interno (mais escuro)
	val corCorpoI = scalafx.scene.paint.Color.rgb(111,12,6);
	//Externo (mais claro)
	val corCorpoE = scalafx.scene.paint.Color.rgb(231,23,20);
	
	//Cor da cabeça da cobra. Amarelos aleatórios que também achei por aí. heh
	//Interno (mais claro)
	val corCabecaI = scalafx.scene.paint.Color.rgb(254,218,60);
	//Externo (mais escuro)
	val corCabecaE = scalafx.scene.paint.Color.rgb(205,112,0);
	
	//Cor da comida da cobra. Cinzas aleatórios.
	//Interno (mais claro)
	val corComidaI = scalafx.scene.paint.Color.rgb(218,209,200);
	//Externo (mais escuro)
	val corComidaE = scalafx.scene.paint.Color.rgb(126,118,127);
	
	//Cor do fundo. Cinza, quase preto.
	val corFundo = scalafx.scene.paint.Color.rgb(32,33,42);
	//Cor da grade. Verde muito escuro, um pouco de azul.
	val corGrade = scalafx.scene.paint.Color.rgb(9,66,28);
	
	//Cor da vitória.
	val corVitoria = scalafx.scene.paint.Color.rgb(200,220,250);
	
	//Cor da derrota.
	val corDerrota = scalafx.scene.paint.Color.rgb(255,20,10);
	
	//===================================================================================
	
	//Calcula dimensões da tela.
	val larguraTela = larguraTabuleiro*tamanhoGrade;
	val alturaTela = alturaTabuleiro*tamanhoGrade;
	
	//Guarda posição atual da comida.
	var posComida:Point2D = null;
	
	//Janela
	var jan:Janela = null;
	
	//Cobra
	var cobra = new Cobrinha(new Point2D(larguraTabuleiro,alturaTabuleiro),new Point2D(larguraTabuleiro/2,alturaTabuleiro/2));
	
	//Timer do jogo
	val tim = new java.util.Timer();
	var milis:Int = 0;
	var proximaChamada = 0;
	val intervaloTimer = 25; 
	var pausado:Boolean = false;
	
	//Método que desenha a cobrinha em todo seu comprimento, e com cores diferentes para a cabeça e corpo.
	def DesenhaCobrinha(){
		
		var t = 0;
		var ponto:Point2D = null;
		
		//Desenhar corpo da cobra.
		for(t <- 1 to cobra.comprimento-1){
			ponto = cobra.ObterPosicao(t);
			jan.DesenhaRetanguloDecorado((ponto.x*tamanhoGrade+1).toInt, (ponto.y*tamanhoGrade+1).toInt,tamanhoGrade-2,tamanhoGrade-2, corCorpoE, corCorpoI);
		}
		
		//Desenhar cabeça da cobra
		ponto = cobra.ObterPosicao(0);
		jan.DesenhaRetanguloDecorado((ponto.x*tamanhoGrade+1).toInt, (ponto.y*tamanhoGrade+1).toInt,tamanhoGrade-2,tamanhoGrade-2, corCabecaE, corCabecaI);
	}
	
	//Desenha a comida da cobra no tabuleiro.
	def DesenhaComida():Unit={
		jan.DesenhaRetanguloDecorado((posComida.x*tamanhoGrade+1).toInt, (posComida.y*tamanhoGrade+1).toInt,tamanhoGrade-2,tamanhoGrade-2, corComidaE, corComidaI);
	}
	
	//Insere uma comida em um lugar vazio aleatório do tabuleiro.
	def InsereComida():Unit={
		
		var qtdPosDisponiveis:Int =  cobra.comprimentoMaximo-cobra.comprimento;
		var indiceAt = 0;
		var x = 0;
		var y = 0;
		var indiceComida = 0;
		var ponto:Point2D = null;
		
		if(qtdPosDisponiveis<=0){
			//Impossível colocar comida.
			return;
		}
		
		indiceComida = (new scala.util.Random()).nextInt(qtdPosDisponiveis);
		posComida = new Point2D(-1,-1);
		
		//Percorrer todo tabuleiro, buscando a posição selecionada.
		//A cada elemento que não pertence à cobra, indiceAt é incrementado.
		//Isso é repetido até indiceAt ser igual a indiceComida.
		//A razão disso é para garantir que a comida caia fora da cobra logo com apenas um número aleatório.
		for(y <- 0 to alturaTabuleiro-1){
			for(x <- 0 to larguraTabuleiro-1){
				ponto = new Point2D(x,y);
				if(cobra.Pertence(ponto)==false){
					if(indiceAt==indiceComida){
						//Encontrou a posição.
						posComida = new Point2D(x,y);
						indiceComida = -1;
						return;
					}else{
						indiceAt+=1;
					}
				}
			}
		}
	}
	
	//Realiza processamento de cada quadro.
	val processaQuadro = new java.util.TimerTask{ 
		def run():Unit={
			//Mede o tempo passado desde o começo.
			milis += intervaloTimer;
			
			//Verifica se a tecla de pausa está ativada.
			if(jan.teclaPausa==true){
				if(cobra.estado==0){
					if(pausado==true){
						//Se já estiver pausado, despausar e redesenhar tabuleiro.
						pausado = false;
						//Agenda.
						proximaChamada = milis+intervaloLento.toInt;
						jan.LimpaTela(corFundo);
						jan.DesenhaGrade(tamanhoGrade, tamanhoGrade, corGrade);
						DesenhaComida();
						DesenhaCobrinha();
					}else{
						pausado = true;
					}
					//Limpa entradas de teclado.
				}else{
					intervaloLento = baseIntervaloLento;
					cobra = new Cobrinha(new Point2D(larguraTabuleiro,alturaTabuleiro), new Point2D(larguraTabuleiro/2,alturaTabuleiro/2));
					InsereComida();
					pausado = false;
					proximaChamada = milis;
				}
				jan.LimpaEntradas();
			}
			
			//Se estiver pausado, desenha tela de pause e sai.
			if(pausado==true){
				jan.LimpaTela(corFundo);
				jan.DesenhaGrade(tamanhoGrade, tamanhoGrade, corGrade);
				jan.DesenhaRetangulo(10,alturaTela*0.3+10,larguraTela-20, alturaTela*0.4-20, scalafx.scene.paint.Color.rgb(64,64,65));
				jan.DesenhaTextaoCentralizado("Pausado...", larguraTela/2, alturaTela/2, scalafx.scene.paint.Color.White, "Arial", (alturaTela*0.4-20).toInt);
				return;
			}
			
			//Esperar chegar na próxima chamada...
			if(milis<proximaChamada){
				return;
			}
			
			//Verifica o estado da cobrinha, decidindo se está viva, morta ou vitoriosa.
			if(cobra.estado==0){
				//Tenta atribuir a direção da cobra.
				cobra.direcao = jan.direcao;
				//Se na próxima posição da cabeça da cobra tiver comida, alimentar e sortear nova posição da comida.
				if(cobra.ObterProximaPosicao()==posComida){
					cobra.Alimenta();
					cobra.Movimenta();
					InsereComida();
					//Aumentar dificuldade.
					intervaloLento = intervaloLento * proporcaoComida;
				}else{
					//Só mover a cobra.
					cobra.Movimenta();
				}
				
				//Desenhar tudo.
				jan.LimpaTela(corFundo);
				jan.DesenhaGrade(tamanhoGrade, tamanhoGrade, corGrade);
				DesenhaComida();
				DesenhaCobrinha();
				
				//Verifica condição atual da cobra e decide como agendar a chamada.
				if(cobra.estado!=0){
					proximaChamada = milis;
				}else{
					//Se o modo CORRE (dash) estiver ativado, fazer o intervalo entre passos ficar menor.
					if(jan.teclaCorre==true){
						proximaChamada += (intervaloLento*proporcaoIntervaloRapido).toInt;
					}else{
						proximaChamada += intervaloLento.toInt;
					}
				}
					
			}else if (cobra.estado==1){
				//Vitória! :)
				jan.DesenhaRetangulo(0,alturaTela*0.3,larguraTela, alturaTela*0.4, scalafx.scene.paint.Color.Black);
				jan.DesenhaRetangulo(10,alturaTela*0.3+10,larguraTela-20, alturaTela*0.4-20, corVitoria);

				jan.DesenhaTextaoCentralizado("Você venceu!", larguraTela/2, alturaTela/2, scalafx.scene.paint.Color.Black, "Comic Sans", (alturaTela*0.4-20).toInt);
				
			}else{
				//Derrota! :(
				jan.DesenhaRetangulo(0,alturaTela*0.3,larguraTela, alturaTela*0.4, scalafx.scene.paint.Color.Black);
				jan.DesenhaRetangulo(10,alturaTela*0.3+10,larguraTela-20, alturaTela*0.4-20, corDerrota);
				
				jan.DesenhaTextaoCentralizado("Eita :/", larguraTela/2, alturaTela/2, scalafx.scene.paint.Color.White, "Comic Sans", (alturaTela*0.4-20).toInt);
				proximaChamada += intervaloLento.toInt;
			}
		}
	}
	
	val app = new JFXApp{
		//Cria cobra.
		cobra = new Cobrinha(new Point2D(larguraTabuleiro,alturaTabuleiro), new Point2D(larguraTabuleiro/2,alturaTabuleiro/2));
		//Cria janela.
		jan = new Janela("Snake Game", larguraTela, alturaTela);
		//Insere a primeira comida.
		InsereComida();
		
		//Agenda quadro.
		tim.schedule(processaQuadro, 1000, intervaloTimer);
		processaQuadro.run();
	}
	
	def main(args: Array[String]): Unit={
		println("Running Snake Game");
		
		//Inicia tudo.
		app.main(args);
		
		//Termina timers.
		tim.cancel();
		tim.purge();
	}

	

}