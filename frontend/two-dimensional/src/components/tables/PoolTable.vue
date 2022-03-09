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
import {IGameMode,IGameToken,IBall,	ITable,	IStick,ISquare, IPointerLine} from "../../interfaces";
import { loginMixin } from "../../mixins/mixins";
import { useRoute } from "vue-router";
import Chat from "../Chat.vue";

const CANVAS_MAXWIDTH = 1200
const CANVAS_MAX_HEIGHT = 677
export default defineComponent({
	components: { Chat },
	name: "PoolTable",
	mixins: [loginMixin],
	props:["watch"],
	data(){
		return{
			canvas:null,
			poolTableImage:null,			
			ballsRemaining :<Array<IBall>>[],
			cueBall:<IBall>null,
			stick:<IStick>null,
			pointerLine:<IPointerLine>null
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
					this.addMouseListeners()
					this.startReducer()
				}else{
					this.removeMouseListeners()
					this.stopReducer()
				}
			}else if (mutation.type === "rematch" ){			
				this.initTable()			
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
		this.initTable()
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
			//TODO, not init all
			this.initTable()
			
		},
		repaint(){
			//	TODO some kind of debounce function for heavy painting operation
			//Table
			this.renderingContext.drawImage(this.poolTableImage,0 , 0, 4551, 2570, 0, 0, this.canvas.width, this.canvas.height)
			//Stick
			this.renderingContext.drawImage(this.stick.image,0 , 0, 4551, 2570, this.stick.positionX , this.stick.positionY, this.canvas.width, this.canvas.height)
			const ballDiameter=this.cueBall.diameter
			//Balls
			for(let i=0;i<this.ballsRemaining.length;i++){
				this.renderingContext.drawImage(this.ballsRemaining[i].image, 0 , 0, 141,141, this.ballsRemaining[i].positionX, this.ballsRemaining[i].positionY,ballDiameter,ballDiameter);
			}
			//Cue
			this.renderingContext.drawImage(this.cueBall.image, 0 , 0, 141,141, this.cueBall.positionX, this.cueBall.positionY,ballDiameter,ballDiameter);
		},
		initTable() {
			setTimeout(()=>{
			console.log("temp temp temp, todo todo todo ")
			this.stick= <IStick>{positionX:100,positionY:100,image:document.getElementById("stickImg")}
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
				canvasWidth = CANVAS_MAXWIDTH
				canvasHeight = CANVAS_MAX_HEIGHT
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
			
				
			this.canvas.height = canvasHeight
			this.canvas.width = canvasWidth
			this.poolTableImage = document.getElementById("tableImg")
		
			window.addEventListener("resize", this.resize)
			const ballWidth = 16
			const ballHeight = 16			
			this.cueBall= <IBall>{
							diameter:ballDiameterPx,			
							positionX:this.canvas.width*0.25,
							positionY:this.canvas.height/2,
							number:0,
							color:"white",
							image:document.getElementById("0")
			}		
			
			for (let i=0; i<16; i++){
				
				if(i===0){
					 // position 0 = cue ball
					continue
				}
				let ball=<IBall>(this.createBall(i,ballDiameterPx))
				this.ballsRemaining.push(this.createBall(i,ballDiameterPx))	
			}
			this.addMouseListeners()
			this.repaint()
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
							diameter:ballDiameterPx,
							positionX:this.canvas.width*0.65+(ballDiameterPx*0.90*this.calculateRackColumn(ballNumber)),
							positionY:this.canvas.height/2-(ballDiameterPx*0.5*this.calculateRow(ballNumber)),
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
		removeMouseListeners(){
			this.canvas.removeEventListener("click", this.handleClick, false);
		},
		addMouseListeners(){
			this.canvas.addEventListener("click", this.handleMouseClick, false)
			this.canvas.addEventListener("mousemove", this.handleMouseMove) 
			this.canvas.addEventListener("mousedown", this.handleMouseDown) 
			this.canvas.addEventListener("mouseup", this.handleMouseUp) 
		
		},	
		drawToken(square: ISquare,color:string) {			
			
		},
		handleMouseClick(event: MouseEvent) {
			console.log("click")
		},
		handleMouseDown(event:MouseEvent){
			console.log("Mouse down")
		},
		handleMouseUp(event:MouseEvent){
			console.log("Mouse up")
		},
		handleMouseMove(event:MouseEvent){
			console.log("event:"+event.offsetX +" " +event.offsetY)
		//	this.removePointerLine()
		//	this.drawPointerLine(event)
			this.changeStickPosition(event)
		// This is heavy operations
		this.repaint()
		},
		changeStickPosition(event:MouseEvent){
			this.stick.positionX = event.offsetX
			this.stick.positionY = event.offsetY
		},
		removePointerLine(){
			const pl = this.pointerLine
			if(pl)
			{
				console.log("Removing_"+JSON.stringify(pl))
				this.renderingContext.clearRect(pl.fromX,pl.fromY,pl.toX,pl.toY);	
			}			
		},
		drawPointerLine(event:MouseEvent){
			if(!event){
				console.log("No event:")
				return
			}
			this.renderingContext.beginPath();	
			this.renderingContext.setLineDash([5, 15]);
			const cueBall = this.cueBall
			console.log("CueBall:"+JSON.stringify(cueBall))
			this.renderingContext.moveTo(cueBall.positionX+cueBall.diameter/2, cueBall.positionY+cueBall.diameter/2)
			this.renderingContext.lineTo(event.offsetX, event.offsetY);
			this.renderingContext.stroke();
			this.pointerLine= {
								fromX:cueBall.positionX+cueBall.diameter/2,
								fromY:cueBall.positionY+cueBall.diameter/2,
								toX:event.offsetX,
								toY:event.offsetY,
							}
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
