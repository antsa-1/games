
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
import {IPoolTable, ICue,IBall,IPockets,IPocket, IEightBallGame,IVector2, IGameImage,IPoolComponent, IEightBallGameOptions, IPoolTableBoundries} from "../../interfaces/pool";
import { loginMixin, } from "../../mixins/mixins";
import { tablesMixin, } from "../../mixins/tablesMixin";
import { useRoute } from "vue-router";
import Chat from "../Chat.vue";

const CANVAS_MAX_WIDTH = 1200
const CANVAS_MAX_HEIGHT = 677
const CUE_MAX_WIDTH = 900
const CUE_MAX_HEIGHT = 12
const BALL_DIAMETER = 34
const SECOND_IN_MILLIS = 1000
const FRICTION = 0.98
let cueForceInterval = undefined
const DELTA = 1/2

export default defineComponent({
	components: { Chat },
	name: "PoolTable",
	mixins: [loginMixin,tablesMixin],
	props:["watch"],
	data():IEightBallGame{
		return{
				canvas: undefined,	
				ballsRemaining : [],
				cueBall: undefined,
				cue: undefined,
				poolTable: undefined,
				gameOptions: undefined
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
			ballsMoving(){
				return this.ballsRemaining.filte
			}
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
		repaintComponent(component: IPoolComponent){
			
			//console.log("Painting:"+JSON.stringify(component))
			const image = component.image
			if( !component.image.visible){
				return
			}
			this.renderingContext.translate(component.position.x, component.position.y)
			if(image.canvasRotationAngle){				
				this.renderingContext.rotate(image.canvasRotationAngle)
			}
		
			//void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
			if(!image.canvasDestination){
				image.canvasDestination = <IVector2> {x:0, y:0}
			}
			if(this.gameOptions && this.gameOptions.helperOrigo && component.hasOwnProperty('force')){

				this.renderingContext.beginPath()
				this.renderingContext.moveTo(-200, 0)
				this.renderingContext.lineTo(200, 0)
				this.renderingContext.stroke()
				
				this.renderingContext.beginPath()
				this.renderingContext.moveTo(0, -200)
				this.renderingContext.lineTo(0, 200)
				this.renderingContext.stroke()
				
			}
			this.renderingContext.drawImage(image.image, 0, 0, image.realDimension.x, image.realDimension.y, image.canvasDestination.x, image.canvasDestination.y, image.canvasDimension.x, image.canvasDimension.y)			
			this.renderingContext.resetTransform()
			this.renderingContext.fillText("O", 0, 0)
		},
		repaintAll(){
			
			this.renderingContext.clearRect(0, 0, this.canvasWidth, this.canvasHeight)
			//Table
			const t = this.poolTable.image
			this.repaintComponent(this.poolTable)
			this.repaintComponent(this.cueBall)
			
			this.repaintComponent(this.cue)
			for( let i = 0; i < this.ballsRemaining.length; i++){
				this.repaintComponent(this.ballsRemaining[i])
			}
			
			/*
				void ctx.drawImage(image, dx, dy);
				void ctx.drawImage(image, dx, dy, dWidth, dHeight);
				void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
			*/			
			//this.renderingContext.fillText("O", 0, 0)
	
		},
		initTable() {
			setTimeout( () => {
			
			
			let windowWidth = window.innerWidth
			let height = window.innerHeight
			
			const table: ITable = this.theTable;
			this.canvas = document.getElementById("canvas");
			this.renderingContext = this.canvas.getContext("2d",{ alpha: false });
			let canvasWidth = windowWidth;
			let canvasHeight = height
			canvasWidth = CANVAS_MAX_WIDTH
			canvasHeight = CANVAS_MAX_HEIGHT
			this.canvas.height = canvasHeight
			this.canvas.width = canvasWidth
			window.addEventListener("resize", this.resize)
			let dimsPoolTable: IVector2 = {x: CANVAS_MAX_WIDTH, y: CANVAS_MAX_HEIGHT}
			let poolTableImage = <IGameImage> {
												image: <HTMLImageElement>document.getElementById("tableImg"), 
												canvasDimension: dimsPoolTable,
												realDimension: {x:4551 ,y: 2570},
												canvasRotationAngle: { x:0, y:0 },
												visible: true										
			}
			const poolTablePockets = <IPockets> {topLeft:{middle:{x:80, y:80}}}
			this.poolTable = <IPoolTable> {image: poolTableImage, position: <IVector2> {x:0, y:0}, boundries:<IPoolTableBoundries>{top: 80, right: 1120, bottom: 600, left:80}, mouseEnabled: true,pockets:poolTablePockets}
			let dimsCueBall: IVector2 = {x: BALL_DIAMETER, y: BALL_DIAMETER}
			let cueBallImage = <IGameImage> {
												image: <HTMLImageElement>document.getElementById("0"),
												canvasDimension: dimsCueBall,
												realDimension: {x: 141, y: 141},
												canvasRotationAngle: { x:0, y:0 },
												canvasDestination:{x:- BALL_DIAMETER/2, y: -BALL_DIAMETER/2},
												visible: true
			}
			this.cueBall = <IBall>{
												image: cueBallImage,
												diameter: BALL_DIAMETER,
												radius: BALL_DIAMETER/2,
												position: <IVector2> {x: 311, y: Math.floor(this.canvas.height / 2)}, //311 is hardcoded for testing purposes
												number:0,
												color:"white",
												velocity:<IVector2>{ x:0, y:0},
												moving: false,
			}
			let dimsCue: IVector2 ={x: CUE_MAX_WIDTH, y: CUE_MAX_HEIGHT}
			let cueImage = <IGameImage> {
												image: <HTMLImageElement>document.getElementById("cue3Img"),
												canvasDimension: dimsCue,
												realDimension:{ x: 1508, y: 22},
												canvasDestination:{x: (-dimsCue.x-BALL_DIAMETER), y: -dimsCue.y/2},
												canvasRotationAngle: { x:0, y:0 },
												visible: true
			}
			let cuePosition = <IVector2> { x: this.cueBall.position.x, y: this.cueBall.position.y }	//5	
			let cueForce = 0
			this.cue = <ICue> {position: cuePosition, image: cueImage, force: cueForce}
			for (let i = 0; i < 16; i++){
				if(i !== 0){
					let ball = <IBall> (this.createBall(i, BALL_DIAMETER))
					this.ballsRemaining.push(this.createBall(i, BALL_DIAMETER))					
				}
			}
			this.gameOptions = <IEightBallGameOptions> { helperOrigo: true}
			this.addMouseListeners()
			window.requestAnimationFrame(this.repaintAll)
		}, 300)	
		},

		startReducer(){			
			if( this.redurcerInterval ){
				this.stopReducer()
			}
			this.secondsLeft = 120;
			this.redurcerInterval = setInterval(() => {
				this.secondsLeft = this.secondsLeft -1
				if(this.secondsLeft <= 0){
					this.stopReducer()
					this.resign()
				}
			}, 1000)
		},
		createBall(ballNumber:number, ballDiameter:number){
			const dims: IVector2 = {x: ballDiameter, y: ballDiameter}
			let position:IVector2 =  {
										x: this.canvas.width * 0.65 +(ballDiameter * 0.90 * this.calculateRackColumn(ballNumber)),
										y: this.canvas.height /2 +(ballDiameter * 0.5 * this.calculateRackRow(ballNumber)),
			}
			let ballImage = <IGameImage> {
							image: <HTMLImageElement>document.getElementById(ballNumber.toString()),
							canvasDimension: dims,
							realDimension: {x: 141, y: 141},
							canvasRotationAngle: { x:0, y:0 },
							canvasDestination:{x:- ballDiameter/2, y: -ballDiameter/2},
							visible: true
			}
			return <IBall>{
							image: ballImage,
							diameter: ballDiameter,
							radius: ballDiameter /2,
							position: position,
							number: ballNumber,
							color: this.calculateBallColor(ballNumber),
			}
		},	
		calculateBallColor(ballNumber:number){
			if(ballNumber===8){
				return "black"
			}
			return ballNumber %2 ===0 ? "red":"yellow"
		},
		calculateRackRow(i){		
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
			this.canvas.addEventListener("mousemove", this.handleMouseMove)
			this.canvas.addEventListener("mousedown", this.handleMouseDown)
			this.canvas.addEventListener("mouseup", this.handleMouseUp)
		},	
		handleMouseDown(event:MouseEvent){
			if(this.poolTable.mouseEnabled){
				cueForceInterval = setInterval(this.updateCueForce, 50)
			}
		},	
	
		handleMouseUp(event:MouseEvent){
			if(this.poolTable.mouseEnabled){
				clearInterval(cueForceInterval)
				this.shootBall()
			}
		},
		handleCollision(component:IPoolComponent){

		},
		handleMouseMove(event:MouseEvent){
			
			if(!this.cueBall.moving){
				
				this.cue.position.x = this.cueBall.position.x
			
				this.cue.position.y = this.cueBall.position.y
				
				this.cue.image.visible = true
				this.mousePoint = <IVector2> {x: event.offsetX, y: event.offsetY }
				if(this.cue.image.visible){
					this.updateCueAngle(event)
					window.requestAnimationFrame(this.repaintAll)
				}
				//	this.updatePointerLine(event)				
			}			
		},
		
		shootBall(){
			console.log("Shoot:"+this.cue.force +" angle:"+this.cue.image.canvasRotationAngle)
			this.poolTable.mouseEnabled = false
			if(this.cue.force < 10 ){
				this.cue.force = 10
			}
			let dimensions: IVector2 = {x: -CUE_MAX_WIDTH-BALL_DIAMETER/2, y: -CUE_MAX_HEIGHT /2}
			this.cue.image.canvasDestination = dimensions
			this.cueBall.velocity = <IVector2>{x : this.cue.force * Math.cos(this.cue.image.canvasRotationAngle),y: this.cue.force * Math.sin(this.cue.image.canvasRotationAngle)}
		
			
			this.collideCueWithCueBall().then(() => {				
				this.cue.image.visible = false
				this.cue.force = 0			
				this.cueBall.moving = true	
				this.handleCollisions().then(() => {
					console.log("Animations should be done now")
					this.cueBall.moving = false
					this.poolTable.mouseEnabled = true
				})
			})
		},
		collideCueWithCueBall(){
			return new Promise((resolve) => {
					this.cue.position = this.cueBall.position
					setTimeout(() => {
						window.requestAnimationFrame(this.repaintAll)
						resolve("movement done")
					}, 145)
			});
		},
		handleCollisions(){
				let component= <IPoolComponent>this.cueBall
				return new Promise((resolve) => {
					const stop = setInterval(() => {
						// Table collisions 
						if(component.position.y <= this.poolTable.boundries.top + this.cueBall.radius){
							//Check if goes into pocket
							if(this.isInPocket(component, this.poolTable.pockets.topLeft)){								
								this.handleBallInPocket(component, this.poolTable.pockets.topLeft)								
							}
							else if(component.position.x > this.poolTable.boundries.left && component.position.x < this.poolTable.boundries.right){
								component.velocity = <IVector2> {x:component.velocity.x, y: -component.velocity.y}
							}
						}
						if(component.position.x >= this.poolTable.boundries.right - this.cueBall.radius){
							
							//Check if goes into pocket
							if(component.position.y >= this.poolTable.boundries.top+ this.cueBall.radius && component.position.y <= this.poolTable.boundries.bottom -this.cueBall.radius){
								component.velocity = <IVector2> {x:-component.velocity.x, y: component.velocity.y}
							}else{
								console.log("Goes into pocket2")
							}
						}
						if(component.position.y >= this.poolTable.boundries.bottom - this.cueBall.radius){
							component.velocity = <IVector2> {x:component.velocity.x, y: -component.velocity.y}
						}
						if(component.position.x <= this.poolTable.boundries.left + this.cueBall.radius){
							component.velocity = <IVector2> {x:-component.velocity.x, y: component.velocity.y}
						}
						component.position.x += component.velocity.x * DELTA
						component.position.y += component.velocity.y * DELTA
						component.velocity.x *= FRICTION // Friction
						component.velocity.y *= FRICTION
						window.requestAnimationFrame(this.repaintAll)
						if(!this.calculateVelocityLength(component).moving){
							clearInterval(stop)							
							resolve("movement done")
						}
					}, 25)
			});
		},
		isInPocket(component:IPoolComponent, pocket:IPocket){
			const ballPercentage = this.cueBall.diameter * 0.65
			const inPocket = component.position.x - ballPercentage <= pocket.middle.x && component.position.y < pocket.middle.y+ ballPercentage
			console.log("InPocket:"+inPocket +" "+ JSON.stringify(component.position)+ " MIDDLE:"+pocket.middle.x+" y:"+pocket.middle.y)
			return inPocket
		},
		handleBallInPocket (component:IPoolComponent, pocket:IPocket){
			component.position = <IVector2> pocket.middle								
			component.image.canvasDimension.x = component.image.canvasDimension.x * 0.5
			component.image.canvasDimension.y = component.image.canvasDimension.y * 0.5
			setTimeout(()=>{
							component.velocity.x = 0
							component.velocity.y = 0
							component.image.canvasDimension.x *= 2
							component.image.canvasDimension.y *= 2
			}, 300)
		},
		calculateVelocityLength(component:IPoolComponent) :IPoolComponent {
			const length = Math.sqrt(Math.pow(component.velocity.x, 2) + Math.pow(component.velocity.y, 2))			
			if(length < 5){
				component.moving = false
				component.velocity = <IVector2> {x: 0, y: 0}
			}
			return component
		},
		updateCueAngle(event:MouseEvent){
			
			let opposite = this.mousePoint.y - this.cueBall.position.y
			let adjacent = this.mousePoint.x - this.cueBall.position.x
		
			const tempAngle = Math.atan2(opposite, adjacent)			
			this.cue.image.canvasRotationAngle = tempAngle	
			window.requestAnimationFrame(this.repaintAll)	
		},
		updateCueForce(){			
			this.cue.force += 5
			this.cue.image.canvasDestination.x  -= 5
			requestAnimationFrame(this.repaintAll)
			
			if(this.cue.force >= 100){
				clearInterval(cueForceInterval)
				this.shootBall()
			}
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