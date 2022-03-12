
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
import {IGameMode,IGameToken,ITable} from "../../interfaces";
import {IPoolTable, ICue,IPosition, IBall,IPointerLine} from "../../interfaces/pool";
import { loginMixin, } from "../../mixins/mixins";
import { tablesMixin, } from "../../mixins/tablesMixin";
import { useRoute } from "vue-router";
import Chat from "../Chat.vue";

const CANVAS_MAX_WIDTH = 1200
const CANVAS_MAX_HEIGHT = 677
const CUE_WIDTH = 900
const CUE_HEIGHT = 16
let interval=undefined

export default defineComponent({
	components: { Chat },
	name: "PoolTable",
	mixins: [loginMixin,tablesMixin],
	props:["watch"],
	data():IPoolTable{
		return{
			canvas: undefined,
			poolTableImage: undefined,			
			ballsRemaining : [],
			cueBall: undefined,
			cue: undefined,
			pointerLine: undefined,
			mousePoint: {x:0,y:0},

			
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
	},
	computed: {

	},
	mounted() {	
		this.initTable()
		
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
		repaintAll(){
			
			this.renderingContext.clearRect(0,0,this.canvasWidth,this.canvasHeight)
			//Table
			this.renderingContext.drawImage(this.poolTableImage,0 , 0, 4551, 2570, 0, 0, this.canvas.width, this.canvas.height)			
			const ballDiameter = this.cueBall.diameter
			//Balls
		//	for(let i = 0; i < this.ballsRemaining.length; i++){
		//		this.renderingContext.drawImage(this.ballsRemaining[i].image, 0 , 0, 141,141, this.ballsRemaining[i].position.x, this.ballsRemaining[i].position.y, ballDiameter,ballDiameter);
		//	}
			//Cue ball
			
			this.renderingContext.drawImage(this.cueBall.image,this.cueBall.position.x,this.cueBall.position.y,ballDiameter,ballDiameter)
		
			//Cue
			this.cue.position.x = this.cueBall.position.x
			this.cue.position.y = this.cueBall.position.y
			
			this.renderingContext.translate(this.cueBall.position.x+ballDiameter/2, this.cueBall.position.y+ballDiameter/2)
	
			/*
				void ctx.drawImage(image, dx, dy);
				void ctx.drawImage(image, dx, dy, dWidth, dHeight);
				void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
			*/
			//moves the canvas and its origin x units horizontally and y units vertically on the grid.		
			
			this.renderingContext.fillText("O", 0, 0)
			this.renderingContext.rotate(-this.cue.angle-(88.5* (Math.PI/180)))
			//const test=-2
			
			this.renderingContext.drawImage(this.cue.image,-CUE_WIDTH-ballDiameter, -7,this.cue.origin,CUE_HEIGHT )			
			// Reset current transformation matrix to the identity matrix
			this.renderingContext.setTransform(1, 0, 0, 1, 0, 0);
	
			//Pointer line
		//	this.renderingContext.beginPath()
		//	this.renderingContext.setLineDash([5, 15])
		//	this.renderingContext.moveTo(this.cueBall.position.x, this.cueBall.position.y)
		//	this.renderingContext.lineTo(this.mousePoint.x, this.mousePoint.y)
		//	this.renderingContext.stroke()
			
		},
		initTable() {
			setTimeout(()=>{
			console.log("temp temp temp, todo todo todo ")
			
			let windowWidth = window.innerWidth
			let height = window.innerHeight
			console.log("initial size:"+windowWidth+ "x "+height)
			const table: ITable = this.theTable;
			this.canvas = document.getElementById("canvas");
			this.renderingContext = this.canvas.getContext("2d",{ alpha: false });
			let canvasWidth = windowWidth;
			let canvasHeight = height
			let ballDiameterPx = 20
			let imageScale=1
			if(windowWidth>1400){
				canvasWidth = CANVAS_MAX_WIDTH
				canvasHeight = CANVAS_MAX_HEIGHT
				ballDiameterPx = 34
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
		
		
			window.addEventListener("resize", this.resize)
			const ballWidth = 16
			const ballHeight = 16
			this.cueBall= <IBall>{
							diameter:ballDiameterPx,			
							position:<IPosition>{x:this.canvas.width*0.25, y:this.canvas.height/2},						
							number:0,
							color:"white",
							image:document.getElementById("0")
			}
			const cuePosition = <IPosition>{x:this.cueBall.position.x-CUE_WIDTH, y:this.cueBall.position.y}
			this.cue= <ICue> { position:cuePosition, image:document.getElementById("cue3Img"), width:CUE_WIDTH, height:CUE_HEIGHT, force:0,origin:CUE_WIDTH}
			for (let i=0; i<16; i++){
				
				if(i===0){
					
					continue
				}
				let ball=<IBall>(this.createBall(i,ballDiameterPx))
				this.ballsRemaining.push(this.createBall(i,ballDiameterPx))	
			}
			this.poolTableImage = document.getElementById("tableImg")
			this.addMouseListeners()
			window.requestAnimationFrame(this.repaintAll)
		},300)
	
		},
	
		startReducer(){			
			if(this.redurcerInterval){
				this.stopReducer()
			}
			this.secondsLeft = 120;
			this.redurcerInterval= setInterval(() => {
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
							position:<IPosition>{
								x: this.canvas.width*0.65+(ballDiameterPx*0.90*this.calculateRackColumn(ballNumber)),
								y: this.canvas.height/2-(ballDiameterPx*0.5*this.calculateRow(ballNumber))
							},						
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
		
	
		addMouseListeners(){
			this.canvas.addEventListener("click", this.handleMouseClick, false)
			this.canvas.addEventListener("mousemove", this.handleMouseMove) 
			this.canvas.addEventListener("mousedown", this.handleMouseDown) 
			this.canvas.addEventListener("mouseup", this.handleMouseUp) 
		
		},
		handleMouseClick(event: MouseEvent) {
			
			console.log("click")
		},
		handleMouseDown(event:MouseEvent){
			console.log("Mouse down"+event.offsetX +" " +event.offsetY)			
			interval = setInterval(this.increaseForce, 100)	
		},
		increaseForce(){
			console.log("FORCE:"+this.cue.force)
			this.cue.force=this.cue.force+4
			this.cue.origin-=3
			requestAnimationFrame(this.repaintAll)
			if(this.cue.force >= 100){
				clearInterval(interval)				
				this.shootBall()
			}
		},
		repaintCue(){
			this.renderingContext.translate(this.cueBall.position.x+this.cueBall.diameter/2, this.cueBall.position.y+this.cueBall.diameter/2)
	
			/*
				void ctx.drawImage(image, dx, dy);
				void ctx.drawImage(image, dx, dy, dWidth, dHeight);
				void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
			*/
			//moves the canvas and its origin x units horizontally and y units vertically on the grid.		
			
			this.renderingContext.fillText("O", 0, 0)
			this.renderingContext.rotate(-this.cue.angle-(88.5* (Math.PI/180)))
			//const test=-2
			console.log("CUE-pos:"+JSON.stringify(this.cue))
			this.renderingContext.drawImage(this.cue.image,-CUE_WIDTH-this.cueBall.diameter, -7,this.cue.origin,CUE_HEIGHT )	
		},
		handleMouseUp(event:MouseEvent){
			clearInterval(interval)
			this.shootBall()
		},
		handleMouseMove(event:MouseEvent){
			
			this.mousePoint =<IPosition> {x:event.offsetX, y:event.offsetY}
			this.updateCue(event)
			this.updatePointerLine(event)
			window.requestAnimationFrame(this.repaintAll)
		},
		shootBall(){
		
			if(this.cue.force <10 ){
				this.cue.force = 10
			}
			this.cue.origin=CUE_WIDTH+15
			console.log("Shooting "+ this.cue.force+ " angle:"+this.cue.angle)
		//	const obj = { title: "MOVE", force: this.cue.force, angle: this.cue.angle,curve: 0 }; // Canvas size?
      		//this.user.webSocket.send(JSON.stringify(obj));
			window.requestAnimationFrame(this.repaintAll)
			this.cue.force = 0
		},
      
		updateCue(event:MouseEvent){			
			let opposite = this.cue.position.x -this.mousePoint.x
			let adjacent = this.cue.position.y	-this.mousePoint.y
			this.cue.angle = Math.atan2(opposite, adjacent)	
		},
	
		updatePointerLine(event:MouseEvent){
			if(!event){
				console.log("No event:")
				return
			}
			const cueBall = this.cueBall
			this.pointerLine= {
								fromX:cueBall.position.x+cueBall.diameter/2,
								fromY:cueBall.position.y+cueBall.diameter/2,
								toX:event.offsetX,
								toY:event.offsetY,
							}
		},
	
	},
	
});
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.hidden{
	visibility:hidden
}
</style>


<!--
 * @author antsa-1 from GitHub 
 -->