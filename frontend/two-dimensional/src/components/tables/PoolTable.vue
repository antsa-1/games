<!--
 * @author antsa-1 from GitHub 
 -->
<template>
	<div class="row">
		<div class="col">
			<button v-if="resignButtonVisible" :disabled ="resignButtonDisabled" @click="resign" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Resign
			</button>			
			<button v-if="rematchButtonEnabled" @click="rematch" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Rematch
			</button>
		</div>
	</div>

	<div class="row">
		<div class="col-xs-12 col-sm-4">
			Text
		</div>		
	</div>
	<div class="col-xs-12 col-sm-8">
			 <canvas id="canvas" width="400" height="400" style="border:1px solid" ></canvas>
    	</div>
	<chat :id="theTable.id"> </chat>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {IGameMode,IGameToken,IBall,	ITable,	IUser,ISquare, IWin} from "../../interfaces";
import { loginMixin } from "../../mixins/mixins";
import { useRoute } from "vue-router";
import Chat from "../Chat.vue";


export default defineComponent({
	components: { Chat },
	name: "PoolTable",
	mixins: [loginMixin],
	props:["watch"],
	data(){
		return{
			canvas:null,
			poolTableImage:null,
			
			
			cueImage:null,
			balls:[],
			canvasMaxWidth:1200,
			canvasMaxHeight:677,
			ballsRemaining :<Array<IBall>>[],
		}
	},
	beforeCreated(){
	
	},
	created() {		

		
		this.unsubscribe = this.$store.subscribe((mutation, state) => {
			if (mutation.type === "move") {
					
				this.animate(this.theTable.board[this.theTable.board.length -1])
				this.playMoveNotification()
				this.highlightLastSquareIfSelected()
			}else if (mutation.type === "changeTurn") {
				if(state.theTable.playerInTurn.name === this.userName){
					this.addMouseListener()
					this.startReducer()
				}else{
					this.removeMouseListener()
					this.stopReducer()
				}
			}else if (mutation.type === "rematch" ){			
				this.initBoard()			
			}
			else if (mutation.type === "updateWinner" ){			
				console.log("update winner")
			}else if(mutation.type === "setDraw"){
				this.stopReducer()
				this.removeMouseListener()
			}
    	})
		if(this.theTable.playerInTurn?.name===this.userName){
			this.startTime=120
			this.startReducer()
		}
	},
	computed: {
		tableWidth(){

		},
		tableHeight(){

		},
		timeLeft(){
			return this.secondsLeft
		},
		theTable() :ITable{
			
			return this.$store.getters.theTable;
		},
		iconColor(){
		 	const watchers=this.$store.getters.theTable.watchers
			 if(watchers && watchers.length>1){
				 return "green"
			 }

			 return "#000000"
		},
		watchers(){
			return this.$store.getters.theTable.watchers
		},
		resignButtonDisabled(){
			const table=this.$store.getters.theTable;
			if(this.watch){
			
				return true
			}else if(this.theTable && !this.theTable.playerInTurn){
					
				return true
			}
			else if(this.theTable.playerInTurn.name!==this.userName){
					
				return true
			}else if(this.theTable && this.theTable.board&& this.theTable.board.length<4){
					
				return true
			}
				
			return false
		},
		rematchButtonEnabled(){
				const table=this.$store.getters.theTable;
			 return !this.watch && table.playerInTurn ===null
		},
		resignButtonVisible(){
			return this.userName===this.theTable?.playerA?.name || this.userName===this.theTable?.playerB?.name
		}
	
	},
	mounted() {
		if(!this.$store.getters.theTable){
			
			this.$router.push('/error')
			return;
		}
		this.initBoard()
		this.animate()
		if(this.watch === "1"){
			this.drawBoard()
		}
	},
	beforeUnmount() {
    	this.unsubscribe()
		this.leaveTable()
  	},
	methods: {
		resize(){
			this.initBoard()
		},
		initBoard() {
			setTimeout(()=>{
				console.log("temp temp temp, todo todo todo ")
			
			let windowWidth = window.innerWidth
			let height = window.innerHeight
			console.log("initial size:"+windowWidth+ "x "+height)
			const table: ITable = this.theTable;
			this.canvas = document.getElementById("canvas");
			this.renderingContext = this.canvas.getContext("2d");
			let canvasWidth = windowWidth;
			let canvasHeight = height
			let ballDiameterPx = 20
			let imageScale=1
			if(windowWidth>1400){
				canvasWidth = this.canvasMaxWidth
				canvasHeight = this.canvasMaxHeight
				ballDiameterPx = 35
			}
			else if(windowWidth >= 992 ){
				canvasWidth=700
				canvasHeight=395
				let imageScale=0.75
				ballDiameterPx = 30				
				console.log("canvasWidth change:"+windowWidth +" x "+height)
			}else if(windowWidth>768 && windowWidth < 992){
				canvasWidth=500
				canvasHeight=282
				console.log("canvas change2:"+windowWidth +" x "+height)
				let imageScale=0.65
				ballDiameterPx = 21
			}else{
				canvasWidth=300
				canvasHeight=169
				console.log("canvas change3:"+windowWidth +" x "+height)
				let imageScale=0.5
				ballDiameterPx = 13 // Not properly visible
			}		
			console.log("Final width:"+windowWidth+" x "+height)
			//Sprite is missing where balls 1-16, -> temp?		
			this.canvas.height = canvasHeight;
			this.canvas.width = canvasWidth;			
			this.poolTableImage = document.getElementById("tableImg");
			this.cueImage = document.getElementById("cueImage");
			this.balls.push(document.getElementById("0")) // cue_ball
			this.balls.push(document.getElementById("1"))
			this.balls.push(document.getElementById("2"))
		
			this.balls.push(document.getElementById("3"))
			this.balls.push(document.getElementById("4"))
			this.balls.push(document.getElementById("5"))
			this.balls.push(document.getElementById("6"))
			this.balls.push(document.getElementById("7"))
			this.balls.push(document.getElementById("8"))
			this.balls.push(document.getElementById("9"))
			this.balls.push(document.getElementById("10"))
			this.balls.push(document.getElementById("11"))
			this.balls.push(document.getElementById("12"))
			this.balls.push(document.getElementById("13"))
			this.balls.push(document.getElementById("14"))
			this.balls.push(document.getElementById("15"))
		
			window.addEventListener("resize", this.resize)
			const ballWidth = 16
			const ballHeight = 16
			//ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
		 	this.renderingContext.drawImage(this.poolTableImage,0 , 0, 4551, 2570, 0, 0, canvasWidth, canvasHeight);
			this.renderingContext.drawImage(this.cueImage,0 , 0, 1447, 814, 0, 0, canvasWidth*0.5, 250);
			
			for (let i=0; i<16; i++){
				console.log("i:"+i)
				
					this.ballsRemaining.push(<IBall>{
										widthPx:ballWidth,
										heightPx:ballHeight,
										relativePositionX:canvasWidth *0.75 - (i*ballWidth),
										relativePositionY:canvasWidth *0.75 + (i*ballHeight),
										number:i
									})
							
				this.renderingContext.drawImage(this.balls[i], 0 , 0, 145, 141, 100 +(ballWidth*i), 100 +(ballHeight*i), ballDiameterPx, ballDiameterPx);
			}
		},3000)
		},
		animate(){
			
		},
		startReducer(){			
			if(this.redurcerInterval){
				this.stopReducer()
			}
			this.secondsLeft = 120;
			this.redurcerInterval= setInterval(()=>{
				this.secondsLeft = this.secondsLeft-1
				if(this.secondsLeft <= 0){
					this.stopReducer()
					this.resign()
				}
			}, 1000)
		},
		
		stopReducer(){
			clearInterval(this.redurcerInterval)
			this.secondsLeft=null
		},
	
		playMoveNotification(){
			if(this.soundOn){
				const audioCtx = new (window.AudioContext )();

				const oscillator = audioCtx.createOscillator();

				oscillator.type = "sine";
				oscillator.frequency.setValueAtTime(446, audioCtx.currentTime); // value in hertz
				
				oscillator.connect(audioCtx.destination);
				oscillator.start();
				setTimeout(
					()=> {
					oscillator.stop();
					},
					250
				);
			}
		},		
		removeMouseListener(){
			this.canvas.removeEventListener("click", this.handleClick, false);
		},
		addMouseListener(){
			this.canvas.addEventListener("click", this.handleClick, false);
		},
		drawWinningLine(win:IWin){
			
			console.log("win")
		},
	
	
		drawToken(square: ISquare,color:string) {
			
			
		},
		handleClick(event: MouseEvent) {
			console.log("click")
		},
		disableBoard(){
				console.log("disable")
		},
		rematch(){
			
			const obj ={title:"REMATCH", message:""}
			this.user.webSocket.send(JSON.stringify(obj));	
		},
		resign(){
			
			const obj ={title:"RESIGN", message:this.tablea}
			this.user.webSocket.send(JSON.stringify(obj));	
		},
		leaveTable(){
			const obj ={title:"LEAVE_TABLE", message:this.theTable.tableId}
			this?.user?.webSocket?.send(JSON.stringify(obj));
		}
	},
	
});
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.hidden{
	visibility:hidden
}
</style>
