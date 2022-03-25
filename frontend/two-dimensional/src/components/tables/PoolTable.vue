
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
import {IPoolTable, ICue, IBall, IPocket, IEightBallGame, IVector2, IGameImage, IPoolComponent, IEightBallGameOptions, IBoundry, IPathWayBorder} from "../../interfaces/pool";
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
const FRICTION = 0.991
let cueForceInterval = undefined

const DELTA = 1/8 // TODO if any lower the ball goes over to pocket in full speed
let middleAreaTopLeft =<IVector2> {x: 125, y:130}
let middleAreaBottomRight =<IVector2> {x: 1072, y:550}
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
				gameOptions: undefined,
				mouseCoordsTemp: undefined
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
			console.log("****RESIZE")
			//this.initTable()
			
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
			if(false && this.gameOptions && this.gameOptions.helperOrigo && component.hasOwnProperty('force')){

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
		
		
		},
		repaintAll(){
			
			this.renderingContext.clearRect(0, 0, this.canvasWidth, this.canvasHeight)
			//Table
			const t = this.poolTable.image
			this.repaintComponent(this.poolTable)
			this.repaintComponent(this.cueBall)
			
			
			
			/*
				void ctx.drawImage(image, dx, dy);
				void ctx.drawImage(image, dx, dy, dWidth, dHeight);
				void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
					
			*/
		
			
			for( let i = 0; i < this.ballsRemaining.length; i++){
				this.repaintComponent(this.ballsRemaining[i])
			}
			this.repaintComponent(this.cue)
			//this.repaintTableParts()
			//this.repaintPockets()
			//this.repaintPathways()
		},
		
		initTable() {
			console.log("****initTable")
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
			this.poolTable = <IPoolTable> {image: poolTableImage, position: <IVector2> {x:0, y:0}, mouseEnabled: true}
			
			const topLeftPart :IBoundry = <IBoundry>{a:79, b:118, c:572}
			const topRightPart :IBoundry = <IBoundry>{a:79, b:644, c:1090}
			const rightPart :IBoundry = <IBoundry>{a:1122, b:118, c:560}
			const bottomRightPart :IBoundry = <IBoundry>{a:600, b:644, c:1090}
			const bottomLeftPart :IBoundry = <IBoundry>{a:600, b:118, c:572}
			const leftPart :IBoundry = <IBoundry>{a:80, b:118, c:560}
			this.poolTable.pockets =[]
			this.poolTable.topLeftPart = topLeftPart
			this.poolTable.topRightPart = topRightPart
			this.poolTable.rightPart = rightPart
			this.poolTable.bottomRightPart = bottomRightPart
			this.poolTable.bottomLeftPart = bottomLeftPart
			this.poolTable.leftPart = leftPart 
			const topLeftPocket:IPocket = <IPocket> {center: {x: 65, y:61}, radius:32, pathwayLeft:{top:{x:54,y:93}, bottom:{x:80, y:118}}, pathwayRight:{top:{x:96,y:56}, bottom:{x:117, y:77}}}
			const topMiddlePocket:IPocket = <IPocket> {center: {x: 607, y:49}, radius:28, pathwayLeft:{top:{x:580,y:56}, bottom:{x:571, y:79}}, pathwayRight:{top:{x:635,y:56}, bottom:{x:644, y:79}}}
			const topRightPocket:IPocket = <IPocket> {center: {x: 1144, y:61}, radius:32, pathwayLeft:{top:{x:1112,y:56}, bottom:{x:1089, y:79}}, pathwayRight:{top:{x:1144,y:93}, bottom:{x:1122, y:118}}}
			const bottomRightPocket:IPocket = <IPocket> {center: {x: 1144, y:614}, radius:32, pathwayLeft:{top:{x:1091, y:600}, bottom:{x:1112, y:620}}, pathwayRight:{top:{x:1122,y:562}, bottom:{x:1142, y:580}}}
			const bottomMiddlePocket:IPocket = <IPocket> {center: {x: 607, y:630}, radius:28, pathwayLeft:{top:{x:571, y:600}, bottom:{x:580, y:620}}, pathwayRight:{top:{x:644,y:600}, bottom:{x:635, y:620}}}
			const bottomLeftPocket:IPocket = <IPocket> {center: {x: 65, y:614}, radius:32, pathwayLeft:{top:{x:80,y:558}, bottom:{x:54, y:584}}, pathwayRight:{top:{x:118,y:600}, bottom:{x:98, y:620}}}
			this.poolTable.pockets.push(topLeftPocket)
			this.poolTable.pockets.push(topMiddlePocket)
			this.poolTable.pockets.push(topRightPocket)
			this.poolTable.pockets.push(bottomRightPocket)
			this.poolTable.pockets.push(bottomMiddlePocket)
			this.poolTable.pockets.push(bottomLeftPocket)
			this.poolTable.topMiddlePocket = topMiddlePocket
			this.poolTable.topRightPocket = topRightPocket
			this.poolTable.bottomRightPocket = bottomRightPocket
			this.poolTable.bottomMiddlePocket = bottomMiddlePocket
			this.poolTable.bottomLeftPocket = bottomLeftPocket

			const dimsCueBall: IVector2 = {x: BALL_DIAMETER, y: BALL_DIAMETER}
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
												position: <IVector2> {x: 625, y: 150}, //311 is hardcoded for testing purposes
												number:0,
												color:"white",
												velocity:<IVector2>{ x:0, y:0},												
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
			for (let i = 1; i < 16; i++){
				let ball =	this.createBall(i, this.cueBall.diameter)				
				this.ballsRemaining.push(ball)			
			}
			this.ballsRemaining.push(this.cueBall)
			this.gameOptions = <IEightBallGameOptions> { helperOrigo: true}
			this.addMouseListeners()
			window.requestAnimationFrame(this.repaintAll)
		}, 300)
		},
		isTableTopBoundry(component:IPoolComponent){
		
			return component.position.x >= this.poolTable.topLeftPart.b &&  component.position.x <=this.poolTable.topLeftPart.c && component.position.y -this.cueBall.radius <=this.poolTable.topLeftPart.a
				|| component.position.x >= this.poolTable.topRightPart.b &&  component.position.x <=this.poolTable.topRightPart.c && component.position.y -this.cueBall.radius <=this.poolTable.topRightPart.a
		},
		isTableLeftBoundry(component:IPoolComponent){
			return component.position.x <= this.poolTable.leftPart.a+this.cueBall.radius &&  component.position.y >=this.poolTable.leftPart.b && component.position.y  <=this.poolTable.leftPart.c
		},
		isTableRightBoundry(component:IPoolComponent){
			return component.position.x >= this.poolTable.rightPart.a - this.cueBall.radius &&  component.position.y >=this.poolTable.rightPart.b && component.position.y <= this.poolTable.rightPart.c
		},
		isTableBottomBoundry(component:IPoolComponent){
			return component.position.x >= this.poolTable.bottomLeftPart.b && component.position.x <=this.poolTable.bottomLeftPart.c && component.position.y +this.cueBall.radius >=this.poolTable.bottomLeftPart.a
				|| component.position.x >= this.poolTable.bottomRightPart.b && component.position.x <=this.poolTable.bottomRightPart.c && component.position.y +this.cueBall.radius >=this.poolTable.bottomRightPart.a
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
							velocity:<IVector2>{ x: 0, y: 0},
			}
		},	
	
		addMouseListeners(){
			this.canvas.addEventListener("mousemove", this.handleMouseMove)
			this.canvas.addEventListener("mousedown", this.handleMouseDown)
			this.canvas.addEventListener("mouseup", this.handleMouseUp)
			this.canvas.addEventListener("contextmenu",(e)=> e.preventDefault()) 
		},	
		handleMouseDown(event:MouseEvent){
			if(event.button ===2){				
				return
			}
			if(this.poolTable.mouseEnabled){
				cueForceInterval = setInterval(this.updateCueForce, 50)
			}
		},	
	
		handleMouseUp(event:MouseEvent){
			if(event.button ===2){
				this.cueBall.position.x = event.offsetX
				this.cueBall.position.y = event.offsetY
				requestAnimationFrame(this.repaintAll)
				return
			}
			if(event.button ===1){ // wheel or middle button
			
			}
			if(this.poolTable.mouseEnabled){
				clearInterval(cueForceInterval)
				this.shootBall()
			}
		},
		handleMouseMove(event:MouseEvent){
			this.mouseCoordsTemp = <IVector2> {x:event.offsetX, y: event.offsetY}
			if(this.poolTable.mouseEnabled){
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
				this.handleCollisions().then(() => {
					console.log("Animations done ball center position "+JSON.stringify(this.cueBall.position))
					this.poolTable.mouseEnabled = true
				})
			})
		},
		collideCueWithCueBall(){
			return new Promise((resolve) => {
				this.cue.position = this.cueBall.position
				setTimeout(() => {
					window.requestAnimationFrame(this.repaintAll)
					console.log("CollideCueWithBall done" )
					resolve("Cue movement done")
				}, 145)
			})
		},
		handleCollisions(){			
			return new Promise((resolve) => {
				const collisionCheckInterval = setInterval(() => {
					this.updateBalls()
					this.handleBallCollisions()
					window.requestAnimationFrame(this.repaintAll)
					if(!this.isAnyBallMoving()){	
						clearInterval(collisionCheckInterval)	
						resolve("collisions checked")
					}
			}, 25)
			})
		},
		updateBalls(){
			this.ballsRemaining.forEach(ball => {				
				this.calculateLength(ball.velocity)
				ball.position.x += ball.velocity.x * DELTA
				ball.position.y += ball.velocity.y * DELTA
				ball.velocity.x *= FRICTION
				ball.velocity.y *= FRICTION				
			})			
		},		
		isInPocket(component:IBall){
			// https://stackoverflow.com/questions/481144/equation-for-testing-if-a-point-is-inside-a-circle
			// (x - center_x)² + (y - center_y)² < radius²   			
			let pocket:IPocket = this.poolTable.pockets[0]
			let inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){
				console.log("pockets[0]:"+inPocket +JSON.stringify(component.position))
				return pocket
			}
			pocket = this.poolTable.topMiddlePocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){
				console.log("topMiddlePocket:"+inPocket +JSON.stringify(component.position))
				return pocket
			}
			pocket = this.poolTable.topRightPocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){
				console.log("topRightPocket:"+inPocket +JSON.stringify(component.position))
				return pocket
			}
			pocket = this.poolTable.bottomRightPocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){
				console.log("bottomRightPocket:"+inPocket +JSON.stringify(component.position))
				return pocket
			}
			pocket = this.poolTable.bottomMiddlePocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){
				console.log("bottomRMiddlePocket:"+inPocket +JSON.stringify(component.position))
				return pocket
			}
			pocket = this.poolTable.bottomLeftPocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){
				console.log("bottomLeftPocket:"+inPocket +JSON.stringify(component.position))
				return pocket
			}		
		},
		handleBallInPocket (component:IBall, pocket:IPocket){									
			component.image.canvasDimension.x = component.image.canvasDimension.x * 0.8
			component.image.canvasDimension.y = component.image.canvasDimension.y * 0.8
			component.position = <IVector2> {x:pocket.center.x, y:pocket.center.y}
			component.velocity.x = 0
			component.velocity.y = 0		
		},
		handleBallCollisions(){			
			for (let i = 0; i < this.ballsRemaining.length; i++){
				const ball:IBall = this.ballsRemaining[i]
				if(this.isMoving(ball)){
					this.checkAndHandleTableCollision(ball)
				}
				for (let j = i+1; j<this.ballsRemaining.length; j++){
					const firstBall:IBall = this.ballsRemaining[j]
					const secondBall:IBall = this.ballsRemaining[i]
					if(firstBall.inPocket || secondBall.inPocket){
						console.log("breaking loop, in pocket "+firstBall.inPocket)
						break
					}					
					if(this.isMoving(firstBall) || this.isMoving(secondBall) ){
						this.collide(secondBall, firstBall)
					}
				}				
			}
		},
	
		collide(componentA:IBall, componentB:IBall){
			// Mathematics from -> ' JavaScript + HTML5 GameDev Tutorial: 8-Ball Pool Game (part 2) '  from 15:42 ->
			// https://www.youtube.com/watch?v=Am8rT9xICRs
				
			let normalVector = this.subtractVectors(componentA.position, componentB.position)
			const normalVectorLength = this.calculateLength(normalVector)
			if(normalVectorLength > this.cueBall.diameter ){				
				return
			}
			
			const scalar = 1/normalVectorLength
			const unitNormalVector = this.multiplyVector(normalVector, scalar)
			const unitTangentVector = <IVector2> { x: -unitNormalVector.y, y: unitNormalVector.x}
			const v1n = this.dotProduct(unitNormalVector, componentA.velocity)
			const v1t = this.dotProduct(unitTangentVector, componentA.velocity)
			const v2n = this.dotProduct(unitNormalVector, componentB.velocity)
			const v2t = this.dotProduct(unitTangentVector, componentB.velocity)
			let v1nTag = v2n
			let v2nTag = v1n
			v1nTag = <IVector2> {x: unitNormalVector.x * v1nTag, y:unitNormalVector.y * v1nTag}
			const v1tTag = <IVector2> {x: unitTangentVector.x * v1t, y:unitTangentVector.y * v1t}
			v2nTag = <IVector2> {x: unitNormalVector.x * v2nTag, y:unitNormalVector.y * v2nTag}
			const v2tTag = <IVector2> {x: unitTangentVector.x * v2t, y:unitTangentVector.y * v2t}
			componentA.velocity = <IVector2> { x: (v1nTag.x + v1tTag.x) , y: (v1nTag.y + v1tTag.y) }
			componentB.velocity = <IVector2> { x: (v2nTag.x + v2tTag.x) , y: (v2nTag.y + v2tTag.y) }
			const someRandomTestNumberToAvoidOverlapping = 4			
			//minimum translation distance, 
			const mtd = this.multiplyVector(normalVector, (this.cueBall.diameter + someRandomTestNumberToAvoidOverlapping - normalVectorLength) / normalVectorLength)
			// TODO check boundries	?	It will be interesting to see how the other browser with different poolTable size handles these in order to be sync. Might need some re-work
			componentA.position.x += mtd.x * 0.5
			componentA.position.y += mtd.y * 0.5
			componentB.position.x -= mtd.x * 0.5
			componentB.position.y -= mtd.y * 0.5
		},
		isMoving(component:IPoolComponent){
			if(!component)
			return false
			return component.velocity.x !== 0 || component.velocity.y !==0
		},
		checkAndHandleTableCollision(ball:IBall){
		
			if(this.isBallInMiddleArea(ball)){
				//console.log("in middle area"+JSON.stringify(ball))
			}else if(this.isTableTopBoundry(ball) ){
				console.log("Top boundry"+JSON.stringify(ball.position))
				ball.velocity = <IVector2> {x:ball.velocity.x, y: -ball.velocity.y}
			}else if(this.isTableBottomBoundry(ball)){
				console.log("Lower boundry"+JSON.stringify(ball.position))
				ball.velocity = <IVector2> {x:ball.velocity.x, y: -ball.velocity.y}
			}else if(this.isTableLeftBoundry(ball) ){
				console.log("Left boundry"+JSON.stringify(ball.position))
				ball.velocity = <IVector2> {x:-ball.velocity.x, y: ball.velocity.y}
			}else if(this.isTableRightBoundry(ball) ){
				console.log("Right boundry"+JSON.stringify(ball.position))
				ball.velocity = <IVector2> {x:-ball.velocity.x, y: ball.velocity.y}
			}else if(this.checkAndHandlePocketPathwayCollisions(ball)){
				console.log("collided with pathway, that's all we know")
			
			}else{
				const pocket:IPocket = this.isInPocket(ball)
				if(pocket){
					this.handleBallInPocket(ball, pocket)
				}
			}
			this.updatePositionAndVelocity(ball)
		},
		updatePositionAndVelocity(ball:IBall){
			ball.position.x += ball.velocity.x * DELTA
			ball.position.y += ball.velocity.y * DELTA
			ball.velocity.x *= FRICTION 
			ball.velocity.y *= FRICTION
		},
		checkAndHandlePocketPathwayCollisions(ball:IBall){
			for(let i = 0; i < this.poolTable.pockets.length; i++){
				const pocket:IPocket = this.poolTable.pockets[i]				
				if(this.isPathwayBorderCollision(pocket.pathwayRight, ball, i)){					
					let reflectionVector = this.calculateBallVelocityOnPathwayBorderCollision(pocket.pathwayRight, ball)
					console.log("Ball currentVelcity:"+JSON.stringify(ball.velocity)+ " reflection:"+JSON.stringify(reflectionVector))
					ball.velocity = reflectionVector
					return true
				} else if(this.isPathwayBorderCollision(pocket.pathwayLeft, ball, i)){
					let reflectionVector = this.calculateBallVelocityOnPathwayBorderCollision(pocket.pathwayLeft, ball)
					ball.velocity = reflectionVector
					console.log("Ball currentVelcity:"+JSON.stringify(ball.velocity)+ " reflection:"+JSON.stringify(reflectionVector))
					return true
				}				
			}
			return false
		},
		//Pathway to pocket means a separate area in front of the pocket which has two sides thus different angles if ball hits them
		calculateBallVelocityOnPathwayBorderCollision(pathway:IPathWayBorder, ball:IBall){
			// calculations ??
			let normalVector:IVector2 = this.subtractVectors(pathway.bottom, pathway.top)
			const normalVectorLength = this.calculateLength(normalVector)
			const scalar = 1/normalVectorLength
			const unitNormalVector = this.multiplyVector(normalVector, scalar)
			let magnitude = 2 * this.dotProduct(unitNormalVector, ball.velocity)
			let multipliedVector:IVector2 = this.multiplyVector(unitNormalVector, magnitude)
			let reflectionVector:IVector2 = this.subtractVectors(multipliedVector, ball.velocity)
			return reflectionVector
		},
		isPathwayBorderCollision(p:IPathWayBorder, ball:IBall){
			// https://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm  -> answer starting  "No one seems to consider projection ..."			
			if(this.isBallInMiddleArea(ball)){
				console.log("Ball in middle area")
				return false
			}
			//console.log("Ball not in middle area")
			const a:IVector2 = p.top
			const b:IVector2 = p.bottom
			const c:IVector2 = ball.position
			const ac:IVector2 = this.subtractVectors(c,a)
			const ab:IVector2 = this.subtractVectors(b,a)
			const d:IVector2 = this.addVectors(this.projectVectorOnVector(ac, ab), a)
			const ad:IVector2 = this.subtractVectors(d,a)
			const k = Math.abs(ab.x) > Math.abs(ab.y) ? ad.x / ab.x : ad.y / ab.y
			let distance = undefined
			if (k <= 0.0) {
				distance = Math.sqrt(this.hypot2(c, a))
			}else if (k >= 1.0) {
				distance = Math.sqrt(this.hypot2(c, b))
			}
			if(!distance){
				distance = Math.sqrt(this.hypot2(c, d))
			}
			//console.log("Distance to pathWay"+distance)
			return distance <= ball.radius			
		},
		isBallInMiddleArea(ball:IBall){
			const middleAreaTopLeft =<IVector2> {x: 125, y:130}
			const middleAreaBottomRight =<IVector2> {x: 1072, y:550}
			const top:IVector2 = this.subtractVectors(ball.position, middleAreaTopLeft)
			const bottom:IVector2 = this.subtractVectors(middleAreaBottomRight, ball.position)
			if(top.x > 0 && top.y > 0){
				return bottom.x > 0 && bottom.y > 0
			}
		},
		hypot2(vectorA:IVector2, vectorB:IVector2){
			return this.dotProduct(this.subtractVectors(vectorA, vectorB), this.subtractVectors(vectorA, vectorB))
		},
		
		projectVectorOnVector(vectorA:IVector2, vectorB:IVector2){
			const scalar = this.dotProduct(vectorA, vectorB) / this.dotProduct(vectorB, vectorB)
			return <IVector2>{x: scalar * vectorB.x, y: scalar * vectorB.y}
		},			
		calculateLength(vector:IVector2){
			// = same as magnitude of Vector
			const length = Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2))			
			return length
		},
		calculateVectorLength(component:IPoolComponent) :IPoolComponent {
			//Magnitude
			const length = Math.sqrt((Math.pow(component.velocity.x, 2) + Math.pow(component.velocity.y, 2)))			
			if(length < 5){				
				component.velocity = <IVector2> {x: 0, y: 0}
			}
			return component
		},
		multiplyVector(vector:IVector2, scalar:number ): IVector2{
			return <IVector2>{ x: vector.x * scalar, y: vector.y * scalar}
		},
		addVectors(vectorA:IVector2, vectorB: IVector2 ): IVector2{
			return <IVector2>{ x: vectorA.x + vectorB.x, y: vectorA.y + vectorB.y}
		},
		subtractVectors(vectorA:IVector2, vectorB: IVector2 ): IVector2{
			return <IVector2>{ x: vectorA.x - vectorB.x, y: vectorA.y - vectorB.y}
		},
		dotProduct(vectorA:IVector2, vectorB: IVector2 ): number{
			return vectorA.x * vectorB.x + vectorA.y * vectorB.y
		},
	
		isAnyBallMoving(){
			for (let i = 0; i< this.ballsRemaining.length; i++){				
				let comp:IPoolComponent = this.ballsRemaining[i]
				this.calculateVectorLength(comp)
				if( comp.velocity.x !== 0 || comp.velocity.y !== 0){
					return true
				}
			}
			return false
		},
		updateCueAngle(event:MouseEvent){
			let opposite = this.mousePoint.y - this.cueBall.position.y
			let adjacent = this.mousePoint.x - this.cueBall.position.x
			const tempAngle = Math.atan2(opposite, adjacent)
			this.cue.image.canvasRotationAngle = tempAngle
			window.requestAnimationFrame(this.repaintAll)
		},
		updateCueForce(){
			this.cue.force += 10
			this.cue.image.canvasDestination.x  -= 5
			requestAnimationFrame(this.repaintAll)
			
			if(this.cue.force >= 250){
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
			calculateBallColor(ballNumber:number){
			if(ballNumber === 8){
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
		repaintTableParts(poolTable:IPoolTable){
		
			this.renderingContext.moveTo(this.poolTable.leftPart.a, this.poolTable.leftPart.b)
				this.renderingContext.lineTo(this.poolTable.leftPart.a, this.poolTable.leftPart.c)			
				this.renderingContext.stroke()
				this.renderingContext.closePath()
				this.renderingContext.moveTo(this.poolTable.topLeftPart.b, this.poolTable.topLeftPart.a)
				this.renderingContext.lineTo(this.poolTable.topLeftPart.c, this.poolTable.topLeftPart.a)			
				this.renderingContext.stroke()
				this.renderingContext.closePath()
				this.renderingContext.moveTo(this.poolTable.topRightPart.b, this.poolTable.topRightPart.a)
				this.renderingContext.lineTo(this.poolTable.topRightPart.c, this.poolTable.topRightPart.a)			
				this.renderingContext.stroke()
				this.renderingContext.closePath()
				this.renderingContext.moveTo(this.poolTable.rightPart.a, this.poolTable.rightPart.b)
				this.renderingContext.lineTo(this.poolTable.rightPart.a, this.poolTable.rightPart.c)			
				this.renderingContext.stroke()
				this.renderingContext.closePath()
				this.renderingContext.moveTo(this.poolTable.bottomRightPart.b, this.poolTable.bottomRightPart.a)
				this.renderingContext.lineTo(this.poolTable.bottomRightPart.c, this.poolTable.bottomRightPart.a)			
				this.renderingContext.stroke()
				this.renderingContext.closePath()
				this.renderingContext.moveTo(this.poolTable.bottomLeftPart.b, this.poolTable.bottomLeftPart.a)
				this.renderingContext.lineTo(this.poolTable.bottomLeftPart.c, this.poolTable.bottomLeftPart.a)			
				this.renderingContext.stroke()
				this.renderingContext.closePath()
		},
		repaintPockets(){
			this.renderingContext.fillStyle = 'red';
			this.renderingContext.lineWidth = 1;
			this.renderingContext.beginPath();
			this.renderingContext.arc(this.poolTable.pockets[0].center.x, this.poolTable.pockets[0].center.y, this.poolTable.pockets[0].radius, 0, 2 * Math.PI);
			this.renderingContext.stroke();
			this.renderingContext.closePath();
			this.renderingContext.fill()
			this.renderingContext.beginPath();
			this.renderingContext.arc(this.poolTable.topMiddlePocket.center.x, this.poolTable.topMiddlePocket.center.y, this.poolTable.topMiddlePocket.radius, 0, 2 * Math.PI);
			this.renderingContext.stroke();
			this.renderingContext.closePath()
			this.renderingContext.fill()
			this.renderingContext.beginPath();
			this.renderingContext.arc(this.poolTable.topRightPocket.center.x, this.poolTable.topRightPocket.center.y, this.poolTable.topRightPocket.radius, 0, 2 * Math.PI);
			this.renderingContext.stroke();
			this.renderingContext.closePath();
			this.renderingContext.fill()
			this.renderingContext.beginPath();
			this.renderingContext.arc(this.poolTable.bottomRightPocket.center.x, this.poolTable.bottomRightPocket.center.y, this.poolTable.bottomRightPocket.radius, 0, 2 * Math.PI);
			this.renderingContext.stroke();
			this.renderingContext.closePath();
			this.renderingContext.fill()
			this.renderingContext.beginPath();
			this.renderingContext.arc(this.poolTable.bottomMiddlePocket.center.x, this.poolTable.bottomMiddlePocket.center.y, this.poolTable.bottomMiddlePocket.radius, 0, 2 * Math.PI);
			this.renderingContext.stroke();
			this.renderingContext.closePath();
			this.renderingContext.fill()
			this.renderingContext.beginPath();
			this.renderingContext.arc(this.poolTable.bottomLeftPocket.center.x, this.poolTable.bottomLeftPocket.center.y, this.poolTable.bottomLeftPocket.radius, 0, 2 * Math.PI);
			this.renderingContext.stroke();
			this.renderingContext.closePath();
			this.renderingContext.fill()
		},
		repaintPathways(){
			if(this.mouseCoordsTemp){
					this.renderingContext.fillStyle = 'black';
					this.renderingContext.fillText("Mouse: x:"+this.mouseCoordsTemp.x +"  y:"+this.mouseCoordsTemp.y, 150, 50)
					this.renderingContext.fillText("Ball:  x:"+this.cueBall.position.x +"  y:"+this.cueBall.position.y, 150, 40)
				}
						for( let i = 0; i < 6; i++){
					let pathway = this.poolTable.pockets[i].pathwayLeft 
					if(!pathway)
					break // testcode
					this.renderingContext.moveTo(pathway.top.x, pathway.top.y)
					this.renderingContext.lineTo(pathway.bottom.x, pathway.bottom.y)			
					this.renderingContext.stroke()
					this.renderingContext.closePath()
					pathway = this.poolTable.pockets[i].pathwayRight
					this.renderingContext.moveTo(pathway.top.x, pathway.top.y)
					this.renderingContext.lineTo(pathway.bottom.x, pathway.bottom.y)			
					this.renderingContext.stroke()
					this.renderingContext.closePath()			
				}
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


<!--
 * 	@author antsa-1 from GitHub 
  	  Guidance from
  	// Youtube -> ' JavaScript + HTML5 GameDev Tutorial: 8-Ball Pool Game (parts 1 and 2) '->
 	// https://www.youtube.com/watch?v=Am8rT9xICRs
 -->