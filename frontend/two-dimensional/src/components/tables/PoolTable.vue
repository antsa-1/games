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
			this.canvas.height = canvasHeight
			this.canvas.width = canvasWidth
			this.poolTableImage = document.getElementById("tableImg")
			this.cueImage = document.getElementById("cueImage")	
			window.addEventListener("resize", this.resize)
			const ballWidth = 16
			const ballHeight = 16
			//ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
		 	this.renderingContext.drawImage(this.poolTableImage,0 , 0, 4551, 2570, 0, 0, canvasWidth, canvasHeight);
			this.renderingContext.drawImage(this.cueImage,0 , 0, 1447, 814, 0, 0, canvasWidth*0.5, 250);
			//add cue ball just for now to center
			let cueBall= <IBall>{
							widthPx:ballDiameterPx,
							heightPx:ballDiameterPx,
							relativePositionX:this.canvas.width*0.25,
							relativePositionY:this.canvas.height/2,
							number:0,
							color:"white",
							image:document.getElementById("0")
			}
			//this.ballsRemaining.push(cueBall)
			this.renderingContext.drawImage(cueBall.image, 0 , 0, 141,141, cueBall.relativePositionX, cueBall.relativePositionY,ballDiameterPx,ballDiameterPx);
			for (let i=0; i<16; i++){
				
				if(i===0){
					 // position 0 = cue ball
					continue
				}
				let ball=<IBall>(this.createBall(i,ballDiameterPx))
				console.log(JSON.stringify(ball, null,2))
				this.ballsRemaining.push(this.createBall(i,ballDiameterPx))
				////ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
				console.log("I:"+i+ " __"+JSON.stringify(this.ballsRemaining[i-1]))
				this.renderingContext.drawImage(this.ballsRemaining[i-1].image, 0 , 0, 141,141, this.ballsRemaining[i-1].relativePositionX, this.ballsRemaining[i-1].relativePositionY,ballDiameterPx,ballDiameterPx);
			}
	
			
		},300)
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

		createBall(ballNumber:number, ballDiameterPx:number){
				return <IBall>{
							widthPx:ballDiameterPx,
							heightPx:ballDiameterPx,
							relativePositionX:this.canvas.width*0.65+(ballDiameterPx*0.90*this.calculateRackColumn(ballNumber)),
							relativePositionY:this.canvas.height/2-(ballDiameterPx*0.5*this.calculateRow(ballNumber)),
							number:ballNumber,
							color:this.calculateBallColor(ballNumber),
							image:document.getElementById(ballNumber.toString())
					}
		},
		calculateBallColor(ballNumber:number){
			if(ballNumber===8){
				return "black"
			}
			return ballNumber %2 ===0 ? "red":"yellow"
		},
		calculateRow(i){		
			if(i===1 || i===8 || i==5){				
				return 0
			}
			else if( i===5 || i==3 || i===15){				
				return 1
			}else if(i===10 || i===2|| i===15){			
				return -1
			}
			else if(i===4 || i===12){
				return -2
			}else if(i===6 || i===14){
				return 2
			}else if(i===7){
				return 3
			}else if(i===9){
				return -3
			}else if(i===13){
				return -4
			}
			else if(i===11){
				return 4
			}
			return -4
		},
		calculateRackColumn(i){			
			if(i===1){				
				return 1
			}
			else if(i===10 || i===3){				
				return 2
			}else if(i===4 ||  i===8 || i===14 ){				
				return 3
			}else if(  i===7 ||i===9||i===2 ||i===15){				
				return 4
			}			
			return 5
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
