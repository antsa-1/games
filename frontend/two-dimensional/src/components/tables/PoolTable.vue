
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
			<span v-if="playingTurn" class="text-success">Playing turn</span>
			<span v-else-if="theTable.playerInTurn?.name === userName" class="text-success"> It's your turn {{userName}} > time left:{{this.theTable.secondsLeft}}  </span>
			<span v-else-if="theTable?.playerInTurn === null" class="text-success"> Game ended </span>
			<div v-else class="text-danger"> In turn: {{theTable.playerInTurn?.name}}</div>
		</div>
	</div>
	
	<div class="col-xs-12 col-sm-8">
			 <canvas id="canvas" width="400" height="400" style="border:1px solid" :class="{'bg-secondary':theTable.playerInTurn ==null}" ></canvas>
    </div>
	<chat :id="theTable.id"> </chat>
	<div class="row">
		<div class="col-xs-12 col-sm-12">
			Eight ball briefly:<br>
			1. Starting player sets the handball on the left side of the line by clicking the position with mouse.<br>
			2. At first the table is open => players choose either solids or stripes to play with.<br>
			3. Move cue position with mouse. Holding mouse button down gives force to the cue, until maxed out.<br>
			4. Last ball to put into a pocket is eight ball. A pocket selection must be done before pocketing.<br>
			5. Player wins if eight ball is hit first and it goes to the selected pocket while cue ball stays on the table.<br>
			5. Other than starting handball can be placed freely at the table unless it is overlapping other ball.
			<br><br>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent,isProxy,toRaw } from "vue";

import {IPlayer,IChatMessage,ITable} from "../../interfaces/interfaces";
import {IPoolTable, ICue, IBall, IPocket, IEightBallGame, IVector2, IGameImage, IPoolComponent,ITurn, IEightBallGameOptions, IBoundry, IPathWayBorder, ITurnQueue} from "../../interfaces/pool";
import { loginMixin, } from "../../mixins/mixins";
import { tablesMixin} from "../../mixins/tablesMixin";
import { poolMixin} from "../../mixins/poolMixin";
import Chat from "../Chat.vue";

const CANVAS_MAX_WIDTH = 1200
const CANVAS_MAX_HEIGHT = 677
const CUE_MAX_WIDTH = 900
const CUE_MAX_HEIGHT = 12
const BALL_DIAMETER = 34
const FRICTION = 0.991
const DELTA = 1/8

let cueForceInterval = undefined
let collisionCheckInterval = undefined
let reducerInterval = undefined
export default defineComponent({
	components: { Chat },
	name: "PoolTable",
	mixins: [loginMixin, tablesMixin, poolMixin],
	props: ["watch"],
	
	data():IEightBallGame{
		return{
				canvas: undefined,	
				balls : [],
				cueBall: undefined,
				cue: undefined,
				poolTable: undefined,
				gameOptions: undefined,
				mouseCoordsTemp: undefined,
				handBall:false,
				playingTurn: false,
				resultSnapshot:undefined,
				pocketSelection:false,
				selectedPocket:null,
				turnQueue: {turns:[], blocked:false}	
			}
	},
	watch: {
		turnQueueLength: {
      		handler(newValue, oldVal) {
				if(newValue > oldVal){					
					this.consumeTurns()
				}
			}
    	},
		mouseEnabled:{
			handler(newValue, oldVal) {				
				if(newValue && !reducerInterval){
					console.log("watcher:restartReducerInterval")					
					this.restartReducerInterval()
				}else if(!newValue){						
					this.stopReducerInterval()
					this.stopCueInterval()
					reducerInterval = null
				}
			}
		},
		timeRunOut:{
			handler(newValue, oldVal) {					
				if(newValue){
					console.log("watcher:stopreducer")	
					this.stopReducerInterval()					
					if(this.isOngoingGame() && this.isMyTurnInSnapshot()){
						this.resign()
					}
				}
			}
		},
  	},
	created() {	
		this.unsubscribe = this.$store.subscribe((mutation, state) => {
			if (mutation.type === "rematch" ){				
				this.resultSnapshot = null
				this.initTable()
			}
			if (mutation.type === "resign" ){				
				this.theTable.playerInTurn = null
				this.handleGameEnd()
				this.clearTurnQueue()
				this.draw()
			}
    	})
	},
	mounted() {
		this.initTable()
		if(this.watch === "1"){
			this.clearTurnQueue()
			this.updatePocketedBalls(this.theTable.playerABalls)
			this.updatePocketedBalls(this.theTable.playerBBalls, false)
			this.updateRemainingBallPositions(this.theTable.remainingBalls, this.balls)		
			let cueBall:IBall = this.theTable.remainingBalls[this.theTable.remainingBalls.length -1]
			this.cue.position = cueBall.position
			this.unblockQueue()
			this.draw()
		}
		this.unsubscribeAction = this.$store.subscribeAction((action, state) => {
			if(action.type ==="poolUpdate"){
				this.cue.image.canvasRotationAngle = action.payload.pool.cue.angle
				this.cue.angle = action.payload.pool.cue.angle
				this.draw()
				return
			}
			
			if(action.type !== "poolPlayTurn" && action.type !== "poolSetHandBall" && action.type !== "poolSelectPocket" 
				&& action.type !== "poolSetHandBallFail" && action.type !== "poolGameEnded"){					
				return
			}					
			this.resetInstanceVariables()
			this.createTableSnapshot(action)
			if(!this.isDocumentVisible() && this.isTimerRestartRequiredForAction(action) && this.isMe(action.payload.table.playerInTurn.name)){				
			 	this.restartReducerInterval()
			}
			if(!this.isDocumentVisible) {								
				return
			}
			this.produceTurns(action)
			this.draw()		
		})
	},
	computed: {
			turnQueueLength(){				
				return this.turnQueue.turns.length
			},
			mouseEnabled(){				
				const playerInTurn = this.$store.getters.playerInTurn
				if(!playerInTurn){				
					this.draw()
					return false
				}
				if(!this.poolTable){
					return false
				}
				const retVal = this.$store.getters.playerInTurn.name === this.userName && this.poolTable.mouseEnabled
				console.log("computed mouseEnabled:"+retVal)			
				return retVal
			},
			cueVisible(){			
				return this.mouseEnabled
			},
			timeRunOut(){
				return this.theTable.secondsLeft <= 0
			}	
	},

	beforeUnmount() {
    	this.unsubscribe()
		this.unsubscribeAction()
		this.clearIntervals()
		this.removeMouseListeners()
		//window.removeEventListener("resize", this.resize)
		document.removeEventListener("visibilitychange", this.onVisibilityChange)
		this.leaveTable()
  	},
	methods: {
		resetInstanceVariables(){
			this.handBall = false
			this.selectedPocket = null
			this.pocketSelection = false
			this.cue.force = 0	
			this.cue.position = this.cueBall.position
			this.resultSnapshot = null			
		},
		clearIntervals(){
			this.stopCueInterval()
			clearInterval(collisionCheckInterval)
			clearInterval(reducerInterval)
		},
		isTimerRestartRequiredForAction(action){
			const retVal= action.type == "poolPlayTurn" || action.type === "poolSetHandBall" || action.type === "poolSelectPocket"			
			return retVal
		},
		consumeTurns(){
			if(this.turnQueue.turns.length === 0 ){			
				const enabled = this.isMyTurnInStore() ? true: false
				this.poolTable.mouseEnabled = enabled
				console.log("TurnQueue is empty, mouseEnabled "+this.poolTable.mouseEnabled)
				return
			}
			if(this.turnQueue.blocked){
				console.log("TurnQueue is blocked, not consuming")
				return
			}
			this.turnQueue.blocked = true
			const turn:ITurn = this.turnQueue.turns.splice(0, 1)[0]			
			console.log("Starting to consume turn "+JSON.stringify(turn))
			if(turn.shootBall){
				console.log("shootball")	
				this.firstTurnPlayed = true				
				this.shootBall(turn).then(() => {
					console.log("shootball done")
					this.selectedPocket = null
					this.cue.force = 0
					const timeout = this.isMyTurnInStore()? 0 : 1500
					this.unblockQueue(timeout)
					this.playingTurn = false					
				})
			}
			else if(turn.setHandBall){
				
				this.setHandBall(turn)
				const timeout = this.isMyTurnInStore()? 0 : 2000
				this.unblockQueue(timeout)
			} else if(turn.setSelectedPocket != null){
				
				this.selectedPocket = turn.setSelectedPocket
				this.pocketSelection = false
				this.unblockQueue(250)
			} else if(turn.askHandBallPosition){
				
				this.handBall = true
				this.cueBall.image.visible = true
				this.unblockQueue(0)
			}else if(turn.lastTurn){			
				this.handleGameEnd(turn)	
			}
			else if(turn.askPocketSelection){
				
				this.pocketSelection = true
				this.unblockQueue(0)
				
			}else if(turn.changePlayer){
				this.$store.dispatch("changeTurn", turn.nextTurnPlayer).then(() => {
					this.unblockQueue(0)
				})
			}	
		},
		unblockQueue(timeout:number = 0){
			this.turnQueue.blocked = false
			this.draw()
			setTimeout(() => {
				this.consumeTurns()	
			}, timeout)	
		},
		produceTurns(action){
			//Document is visible
			if(action.type === "poolPlayTurn"){
				this.createShootBallTurn(action)
				if(action.payload.pool.turnResult === "CHANGE_TURN"){
					this.createChangePlayerTurn(action)
				}else if(action.payload.pool.turnResult === "HANDBALL"){
					this.createChangePlayerTurn(action)
					if(!this.isMe(action.payload.pool.whoPlayed)){
						this.createAskHandBallPositionTurn(action)
					}					
				}else if(action.payload.pool.turnResult === "ASK_POCKET_SELECTION"){	
					if(this.isTurnChange(action.payload.table.playerInTurn)){
						this.createChangePlayerTurn(action)						
					}
					if(this.isMe(action.payload.table.playerInTurn.name)){
						this.createAskPocketSelectionTurn(action)
					}
				}
			}else if(action.type === "poolSetHandBallFail"){
				this.createAskHandBallPositionTurn(action)
			}else if(action.type === "poolSetHandBall"){				
				this.createPoolSetHandBallTurn(action)
				if(action.payload.pool.turnResult === "ASK_POCKET_SELECTION"){
					if(this.isTurnChange(action.payload.table.playerInTurn)){
						this.createChangePlayerTurn(action)						
					}
					if(this.isMe(action.payload.table.playerInTurn.name)){
						this.createAskPocketSelectionTurn(action)
					}
				}
			}else if(action.type === "poolSelectPocket"){	
				this.createSelectPocketTurn(action)
			}else if(action.type === "poolGameEnded"){				
				this.createLastTurn(action)
			}
		},
		createAskHandBallPositionTurn(action):ITurn{			
			let turn:ITurn = {turnResult:action.payload.pool.turnResult, askHandBallPosition:true, askPocketSelection:false, setSelectedPocket: null, setHandBall:false, lastTurn: false,whoPlayed:action.payload.pool.whoPlayed}
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)
			console.log("produced: createAskHandBallPositionTurn"+this.turnQueue.turns.length)
			return turn
		},
		createAskPocketSelectionTurn(action):ITurn{			
			let selectedPocket = action.payload.pool.selectedPocket
			let nextTurnPlayer:IPlayer = action.payload.table.playerInTurn
			let turn:ITurn = {changePlayer:false, turnResult:action.payload.pool.turnResult, askPocketSelection:true, setSelectedPocket: null, setHandBall:null,nextTurnPlayer:nextTurnPlayer, lastTurn: false,whoPlayed:action.payload.pool.whoPlayed}			
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)
			console.log("produced: createAskPocketSelectionTurn"+this.turnQueue.turns.length)
			return turn
		},
		createSelectPocketTurn(action):ITurn{		
			let selectedPocket = action.payload.pool.selectedPocket
			let turn:ITurn = {turnResult:action.payload.pool.turnResult, askPocketSelection:false, setSelectedPocket: selectedPocket, setHandBall:null, lastTurn: false,whoPlayed:action.payload.pool.whoPlayed}			
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)
			console.log("produced: createSelectPocketTurn"+this.turnQueue.turns.length)
			return turn
		},
		createLastTurn(action):ITurn{
			const turnResult = action.payload.pool.turnResult
			const winner = action.payload.pool.winner.name
			let turn:ITurn = { askPocketSelection:false, setSelectedPocket: null, setHandBall:null, lastTurn:true,winner:winner, winReason:turnResult,whoPlayed:action.payload.pool.whoPlayed}			
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)		
			console.log("produced: createLastTurn"+this.turnQueue.turns.length)
			return turn
		},
		createChangePlayerTurn(action):ITurn{
			let player:IPlayer = action.payload.table.playerInTurn
			let turn:ITurn = {changePlayer:true, nextTurnPlayer:player, lastTurn: false,whoPlayed:action.payload.pool.whoPlayed}			
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)
			console.log("produced: createChangePlayerTurn"+this.turnQueue.turns.length)
			return turn
		},
		createShootBallTurn(action):ITurn{
			let cue:ICue = action.payload.pool.cue
			cue.force = action.payload.pool.cue.force
			cue.angle = action.payload.pool.cue.angle
			let turn:ITurn = {shootBall:true, cue:cue, turnResult:action.payload.pool.turnResult, lastTurn: false, whoPlayed:action.payload.pool.whoPlayed}			
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)
			console.log("produced: createShootBallTurn"+this.turnQueue.turns.length)
			return turn	
		},
		createPoolSetHandBallTurn(action):ITurn{
			let { ...cueBallTemp} = this.cueBall
			cueBallTemp.position = action.payload.pool.cueBall.position
			cueBallTemp.inPocket = false
			cueBallTemp.image.visible = true						
			let cueTemp:ICue = {... this.cue}
			cueTemp.force = 0
			cueTemp.position = cueBallTemp.position		
			let turn:ITurn = {cue:cueTemp,cueBall:cueBallTemp,turnResult:action.payload.pool.turnResult,setHandBall:true,nextTurnPlayer:action.payload.table.playerInTurn, lastTurn: false,whoPlayed:action.payload.pool.whoPlayed}
			this.turnQueue.turns.splice(this.turnQueue.turns.length, 0, turn)
			console.log("produced: createPoolSetHandBallTurn"+this.turnQueue.turns.length)
			return turn			
		},
		handleGameEnd(turn:ITurn){
			this.theTable.playerInTurn = null
			this.clearTurnQueue()
			this.turnQueue.blocked = true
			this.clearIntervals()
			this.removeMouseListeners()
			this.poolTable.mouseEnabled = false
			if(turn){
				const message:IChatMessage = {from:"System",text:turn.winner + " won " + "( "+turn.winReason.toLowerCase() +" )"}					
				this.$store.dispatch("chat", message)
			}
			this.draw()	
		},
		setHandBall(turn:ITurn){
			this.cueBall.position = turn.cueBall.position			
			this.cueBall.inPocket = false
			this.cue.position = this.cueBall.position
			this.handBall = false
			this.cue.force = 0
			this.cueBall.image.visible = true
		},
		createTableSnapshot(action){
			this.firstTurnPlayed = true	
			this.resultSnapshot = action			
		},
		updateRemainingBallPositions(serverBalls:Array<IBall>, comparisonList:Array<IBall>){		
			serverBalls.forEach(serverBall => {
				let	ball:IBall= comparisonList.find((ball) => ball.number === serverBall.number)
				this.updateBallPropertiesWithoutFriction(ball, serverBall.position)
			})
		},
		updateBallPropertiesWithoutFriction(ball:IBall, position:IVector2){
			ball.position.x = position.x
			ball.position.y = position.y
			ball.velocity.x = 0
			ball.velocity.y = 0
		},

		updatePocketedBalls(ballsInPocket:Array<IBall>, playerA = true){			
			if(!ballsInPocket || ballsInPocket.length === 0){
				return
			}
			let startXCoord = playerA? this.canvas.width * 0.12: this.canvas.width * 0.55						
			for(let i = 0; i < ballsInPocket.length; i++){
				let balla = ballsInPocket[i]				
				let realBall:IBall = this.balls.find(remainingBall => remainingBall.number === balla.number)
				if(!realBall){
					continue
				}			
				realBall.position.x = startXCoord + i * realBall.diameter
				realBall.position.y = this.canvas.height * 0.06
				//method is run several times if creating table from snapshot
				if(!realBall.inPocket){
					realBall.image.canvasDimension.x = realBall.image.canvasDimension.x * 0.8
					realBall.image.canvasDimension.y = realBall.image.canvasDimension.y * 0.8
				}		
				realBall.inPocket = true				
			}	
		},
		movePocketedBallToEdgeOfTable(ball:IBall){			
			if(ball.number === 8 || ball.number === 0){
				return
			}
			//resultSnapshot data can be ahead of time/turns what is displayed within turns since. Snapshot can contain more pocketed balls than what is displayed in current time
			let index = this.resultSnapshot.payload.table.playerABalls.findIndex(balla => balla.number === ball.number)
			let startX = this.canvas.width * 0.12	
			if(index === -1){
				index = this.resultSnapshot.payload.table.playerBBalls.findIndex(balla => balla.number === ball.number) +1
				startX = this.canvas.width * 0.55
			}
			ball.position.x = startX + index * ball.diameter
			ball.position.y = this.canvas.height * 0.06				
		},
		putBallInMiddleOfPocket(ball:IBall, pocket:IPocket){			
			if(ball.inPocket){
				return
			}
			ball.velocity.x = 0
			ball.velocity.y = 0
			if(pocket){
				//Only in the active browser (animation) pocket exist
				ball.position.x = pocket.center.x
				ball.position.y = pocket.center.y
			}
			ball.inPocket = true
			if(ball.number === 0 || ball.number === 8){				
				return
			}			
			ball.image.canvasDimension.x = ball.image.canvasDimension.x * 0.8
			ball.image.canvasDimension.y = ball.image.canvasDimension.y * 0.8			
		},
		draw(){
			if(this.isDocumentVisible()){
				requestAnimationFrame(this.repaintAll)				
			}
		},
		repaintComponent(component: IPoolComponent){
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
			if(!this.isMyTurnInStore() && component.hasOwnProperty('force')){
				this.renderingContext.drawImage(image.image, 1430, 0, 100, 22,  image.canvasDestination.x+825, image.canvasDestination.y, 88, 10)
			}else {
			 	this.renderingContext.drawImage(image.image, 0, 0, image.realDimension.x, image.realDimension.y, image.canvasDestination.x, image.canvasDestination.y, image.canvasDimension.x, image.canvasDimension.y)			
			}
			this.renderingContext.resetTransform()
		},
		repaintAll(){
			this.renderingContext.clearRect(0, 0, this.canvasWidth, this.canvasHeight)
			//Table
			this.repaintComponent(this.poolTable)
			this.repaintComponent(this.cueBall)
			for( let i = 0; i < this.balls.length; i++){
				this.repaintComponent(this.balls[i])
			}
			if(!this.isOngoingGame()){
				this.repaintGameEnd()
			}
			this.repaintNames()
			if(!this.handBall && !this.playingTurn){
				this.repaintComponent(this.cue)
			}
			if(this.handBall && this.isMyTurnInStore() ){
				this.renderingContext.font = "bolder 16px Arial"
				this.renderingContext.fillText("Set handball position", 400, 35)
			}
			if(this.pocketSelection && this.isMyTurnInStore()){
				const highLightPocket = this.selectedPocket === null ? 0: this.selectedPocket				
				this.repaintPocketSelection(highLightPocket)
				this.renderingContext.font = "bolder 16px Arial"
				this.renderingContext.fillText("Click to select pocket", 400, 35)				
			}else if(this.selectedPocket !==null){
				this.repaintPocketSelection(this.selectedPocket)				
			}
			this.renderingContext.fillStyle = 'black';
			//this.repaintTableParts()
			//this.repaintPockets()
			//this.repaintPathways()
		},
		repaintGameEnd(){
			this.renderingContext.globalAlpha = 0.4
			this.renderingContext.fillStyle = 'gray'
			this.renderingContext.fillRect(0, 0, this.canvas.width, this.canvas.height)
		},
		initTable() {			
			this.balls = []
			this.turnQueue = <ITurnQueue> {turns:[], blocked:false}	
			let windowWidth = window.innerWidth
			let height = window.innerHeight			
			this.canvas = document.getElementById("canvas");
			this.renderingContext = this.canvas.getContext("2d",{ alpha: false });
			let canvasWidth = windowWidth
			let canvasHeight = height
			canvasWidth = CANVAS_MAX_WIDTH
			canvasHeight = CANVAS_MAX_HEIGHT
			this.canvas.height = canvasHeight
			this.canvas.width = canvasWidth
			//window.addEventListener("resize", this.resize)
			document.addEventListener("visibilitychange", this.onVisibilityChange)
			let dimsPoolTable: IVector2 = {x: CANVAS_MAX_WIDTH, y: CANVAS_MAX_HEIGHT}
			let poolTableImage = <IGameImage> {
												image: <HTMLImageElement>document.getElementById("tableImg"), 
												canvasDimension: dimsPoolTable,
												realDimension: {x:4551 ,y: 2570},
												canvasRotationAngle: { x:0, y:0 },
												visible: true										
			}
			this.poolTable = <IPoolTable> {image: poolTableImage, position: <IVector2> {x:0, y:0}, mouseEnabled: false}
			
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
			const bottomMiddlePocket:IPocket = <IPocket> {center: {x: 607, y:628}, radius:28, pathwayLeft:{top:{x:571, y:600}, bottom:{x:580, y:620}}, pathwayRight:{top:{x:644,y:600}, bottom:{x:635, y:620}}}
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
												position: <IVector2> {x:1030, y: 150}, 
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
			if(this.isPlayerInTurn(this.userName)){
				this.handBall = true
				this.poolTable.mouseEnabled = true		
			}
		//	this.restartReducerInterval()
			let cuePosition = <IVector2> { x: this.cueBall.position.x, y: this.cueBall.position.y }	//5	
			let cueForce = 0
			this.cue = <ICue> {position: cuePosition, image: cueImage, force: cueForce, angle:0}
			for (let i = 1; i < 16; i++){
				let ball =	this.createBall(i, this.cueBall.diameter)	
				this.balls.push(ball)
			}
			this.balls.push(this.cueBall)
			this.gameOptions = <IEightBallGameOptions> { helperOrigo: true, useAnimation:true}
			if(!(this.watch === "1")){
				this.removeMouseListeners()
				this.addMouseListeners()
			}
			this.draw()		
		},
		isDocumentVisible(){
			return document.visibilityState === 'visible'
		},
		onVisibilityChange(){
			if (this.isDocumentVisible()) {		
			
				this.stopCueInterval()
				clearInterval(collisionCheckInterval)
				this.createTableFromSnapShot()				
				this.clearTurnQueue()
				this.unblockQueue(0)
				this.draw()
			} else {
				this.turnQueue.blocked = true	
				this.stopCueInterval()
				clearInterval(collisionCheckInterval)
			}			
		},		
		clearTurnQueue(){
			this.turnQueue.turns.splice(0, this.turnQueue.turns.length)
		},
		createTableFromSnapShot(){
		//	console.log("create table from snapshot")
			if(!this.resultSnapshot)	{
				//No events yet
				return
			}
			let table = this.resultSnapshot.payload.table
			let pool = this.resultSnapshot.payload.pool
			this.playingTurn = false
			this.updatePocketedBalls(table.playerABalls)
			this.updatePocketedBalls(table.playerBBalls, false)
			this.updateRemainingBallPositions(table.remainingBalls, this.balls)	
			this.cueBall.position = pool.cueBall.position
			this.cueBall.velocity.x = 0
			this.cueBall.velocity.y = 0	
			let playerInTurn:IPlayer = 	table.playerInTurn
			this.changeTurnIfRequired(playerInTurn)
			if(pool.winner){
				console.log("winner from snapshot"+pool.winner.name)
				const winner = pool.winner.name
				const winReason = pool.turnResult
				let turn:ITurn = {askPocketSelection:false, setSelectedPocket: null, setHandBall:null, lastTurn:true, winner:winner, winReason:winReason,whoPlayed:pool.whoPlayed}
				this.handleGameEnd(turn)
			}
			else if(this.isMyTurnInStore()){
				this.poolTable.mouseEnabled = true
			}
			if(pool.turnResult === "HANDBALL" && this.isMyTurnInStore()){				
				this.handBall = true
			}else if(pool.turnResult === "ASK_POCKET_SELECTION" && this.isMyTurnInStore()){				
				this.pocketSelection = true
			}
			
			requestAnimationFrame(this.repaintAll)
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
		restartReducerInterval(always:boolean){
			//This is just a friendly timer which can be bypassed in UI
			if(reducerInterval){
				this.stopReducerInterval()
			}		
			this.theTable.secondsLeft = this.getTimeControls()[this.theTable.timeControlIndex].seconds		
			reducerInterval = setInterval(() => {
				this.theTable.secondsLeft --			
			}, 1000)
		}, 
		startCueInterval(){
			console.log("called startCueInterval")
			if(cueForceInterval){
				console.log("called startCueInterval stopping first")
				this.stopInterval()
			}
			cueForceInterval = setInterval(this.updateCueForce, 50)
			console.log("called startCueInterval startin really"+cueForceInterval)
			
		},
		stopCueInterval(){
			console.log("stopping cueForceInterval "+cueForceInterval)
			cueForceInterval = clearInterval(cueForceInterval)		
			console.log("stopped cueForceInterval" +cueForceInterval)			
		},
		stopReducerInterval(){
			reducerInterval = clearInterval(reducerInterval)
			console.log("stoppedReducerInterval"+reducerInterval)
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
			this.canvas.onpointerdown = this.handleMouseDown
			this.canvas.onpointerup = this.handleMouseUp
			this.canvas.onpointermove = this.handleMouseMove
			this.canvas.addEventListener("pointerdown", this.handleMouseDown, false);
  			this.canvas.addEventListener("pointerup", this.handleMouseUp, false);
 			//this.canvas.addEventListener("pointercancel", handleCancel, false);
 	 		this.canvas.addEventListener("pointermove", this.handleMouseMove, false);
			this.canvas.addEventListener("contextmenu",(e)=> e.preventDefault())			
		
		},
		removeMouseListeners(){
			this.canvas.removeEventListener("mousemove", this.handleMouseMove)
			this.canvas.removeEventListener("mousedown", this.handleMouseDown)
			this.canvas.removeEventListener("mouseup", this.handleMouseUp)
			this.canvas.removeEventListener("contextmenu",(e)=> e.preventDefault())
			this.canvas.removeEventListener("pointerdown", this.handleMouseUp) 
			this.canvas.removeEventListener("pointerup", this.handleMouseUp) 
			this.canvas.removeEventListener("pointermove", this.handleMouseUp)
		},
		handleMouseDown(event:MouseEvent){
			if(event.button === 2){				
				return
			}			
			if(!this.poolTable.mouseEnabled ){
				return
			}			
			if(this.pocketSelection){			
				return
			}
			else if(this.poolTable.mouseEnabled && ! this.handBall){
				this.cue.force = 0
				console.log("cueForce interval:"+cueForceInterval)
				if(!cueForceInterval){
					this.startCueInterval()
				}
			}
		},	
	
		handleMouseUp(event:MouseEvent){
			if(!this.poolTable.mouseEnabled){
				return
			}			
			this.stopCueInterval()
			if(this.pocketSelection){				
				this.poolTable.mouseEnabled = false
				this.sendPocketSelection(this.selectedPocket)				
				return
			}
			else if(this.handBall){
				this.cueBall.position.x = event.offsetX
				this.cueBall.position.y = event.offsetY				
				this.hb(event.offsetX, event.offsetY, this.canvas)
			}
			else if(!this.handBall){
				this.poolTable.mouseEnabled = false				
				this.stopReducerInterval()				
				this.sendTurn(this.cue, this.cueBall, this.canvas)
			}
			this.poolTable.mouseEnabled = false
		},
	
		isMyTurnInSnapshot(){
			if(!this.resultSnapshot){
				return false
			}
			return this.resultSnapshot.payload.table.playerInTurn.name === this.userName
		},
		isHandBallPositionAllowed(){
			//Starting position position need to be behind the line			
			if(!this.firstTurnPlayed){
				if(this.mouseCoordsTemp.x > this.canvas.width * 0.25) {// relative line position
					return false
				}
			}
			//Table
			if( this.mouseCoordsTemp.x > this.poolTable.leftPart.a + this.cueBall.radius &&  this.mouseCoordsTemp.x < this.poolTable.rightPart.a - this.cueBall.radius){
				if(! (this.mouseCoordsTemp.y > this.poolTable.topLeftPart.a+ this.cueBall.radius && this.mouseCoordsTemp.y < this.poolTable.bottomRightPart.a -this.cueBall.radius) ){
					return false
				}
			}else{
				return false
			}
			//Balls
			for (let i = 0; i < this.balls.length; i++) {		
				const ball:IBall = this.balls[i]
				if(ball.number === 0){
					continue
				}
				let xVal = ball.position.x - this.cueBall.position.x		
				let yVal = ball.position.y - this.cueBall.position.y				
				if(!(Math.abs(xVal) > ball.diameter) && !(Math.abs(yVal) > ball.diameter)){					
					return false					
				}	
			}
			return true
		},
		handleMouseMove(event:MouseEvent){
			if(! this.poolTable.mouseEnabled){
				return
			}
			this.mouseCoordsTemp = <IVector2> {x:event.offsetX, y: event.offsetY}
			if(this.pocketSelection){				
				this.selectedPocket = this.getClosestPocket(event)				
				this.draw()
				return
			}
			if(this.handBall){				
				this.cueBall.position.x = event.offsetX
				this.cueBall.position.y = event.offsetY
				setTimeout(() => {
					if(this.isHandBallPositionAllowed(event)){						
						this.cueBall.image.visible = true
					}else {
						this.cueBall.image.visible = false
					}
					this.draw()
				}, 10)
			}else{
				this.cue.position.x = this.cueBall.position.x
				this.cue.position.y = this.cueBall.position.y
				this.mousePoint = <IVector2> {x: event.offsetX, y: event.offsetY }
				if(this.isMyTurnInStore()){
					setTimeout(() => {
						this.updateCueAngle(event)
						this.draw()
					},150)					
				}
			}
		},
		handleAfterAnimation(){			
			return new Promise((resolve) => {
				if(this.isMyTurnInStore() && this.theTable.playerInTurn !== null){					
					this.cue.force = 0
				}
				this.draw()				
				resolve("resolve after animation")
			})
		},
		shootBall(turn:ITurn){		
			return new Promise((resolve) => {				
				if(this.cue.force < 10 ){
					this.cue.force = 10
				}
				let dimensions: IVector2 = {x: -CUE_MAX_WIDTH - BALL_DIAMETER/2, y: -CUE_MAX_HEIGHT /2}
				this.cue.force = turn.cue.force
				this.cue.angle = turn.cue.angle
				this.cue.image.canvasRotationAngle = turn.cue.angle
				this.cue.image.canvasDestination = dimensions
				this.cueBall.velocity = <IVector2>{x : this.cue.force * Math.cos(this.cue.image.canvasRotationAngle),y: this.cue.force * Math.sin(this.cue.image.canvasRotationAngle)}
				this.collideCueWithCueBall().then(() => {
					this.playingTurn = true								
					this.handleCollisions().then(() => {											
						this.handleAfterAnimation().then(() => {														
							resolve("animations done")
						})
					})
				})
			})
		},
		collideCueWithCueBall(){
			return new Promise((resolve) => {
				this.cue.position = this.cueBall.position
				setTimeout(() => {					
					this.draw()
					resolve("Cue movement done")
				}, 145)
			})
		},
		handleCollisions(){						
			return new Promise((resolve) => {				
					collisionCheckInterval = setInterval(() => {					
					this.updateBallProperties()
					this.handleBallCollisions()
					this.draw()
					if(!this.isAnyBallMoving()){	
						clearInterval(collisionCheckInterval)						
						resolve("collisions checked")
					}
			}, 25)
			})
		},
		updateBallProperties(){			
			for(let i = 0;i <this.balls.length; i++){
				let ball:IBall = this.balls[i]			
				if(ball.inPocket){
					ball.velocity = <IVector2> {x: 0, y: 0}
					continue
				}
				ball.position.x += ball.velocity.x * DELTA
				ball.position.y += ball.velocity.y * DELTA
				ball.velocity.x *= FRICTION
				ball.velocity.y *= FRICTION						
			}	
		},
	
		checkIfGoesToPocket(component:IBall){
			// https://stackoverflow.com/questions/481144/equation-for-testing-if-a-point-is-inside-a-circle
			// (x - center_x)² + (y - center_y)² < radius²   			
			let pocket:IPocket = this.poolTable.pockets[0]
			let inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){			
				return pocket
			}
			pocket = this.poolTable.topMiddlePocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){				
				return pocket
			}
			pocket = this.poolTable.topRightPocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){				
				return pocket
			}
			pocket = this.poolTable.bottomRightPocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){				
				return pocket
			}
			pocket = this.poolTable.bottomMiddlePocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){				
				return pocket
			}
			pocket = this.poolTable.bottomLeftPocket
			inPocket = Math.pow(component.position.x - pocket.center.x, 2) + Math.pow(component.position.y - pocket.center.y, 2) <= Math.pow(pocket.radius, 2)
			if(inPocket){			
				return pocket
			}		
		},		
		handleBallCollisions(){			
			for (let i = 0; i < this.balls.length; i++){
				const ball:IBall = this.balls[i]
				if(this.isMoving(ball) && !ball.inPocket){
					
					this.checkAndHandleTableCollision(ball)
				}
				for (let j = i + 1; j < this.balls.length; j++){
					const firstBall:IBall = this.balls[j]
					const secondBall:IBall = this.balls[i]
					if(firstBall.inPocket || secondBall.inPocket){	
					//	console.log("handleBallCollisions 33")							
						continue
					}
					if(this.isMoving(firstBall) || this.isMoving(secondBall) ){
						this.checkAndHandleCollision(secondBall, firstBall)
					}
				}
			}
		},	
		checkAndHandleCollision(componentA:IBall, componentB:IBall){
			// Mathematics from -> ' JavaScript + HTML5 GameDev Tutorial: 8-Ball Pool Game (part 2) '  from 15:42 ->
			// https://www.youtube.com/watch?v=Am8rT9xICRs
				
			let normalVector = this.subtractVectors(componentA.position, componentB.position)
			const normalVectorLength = this.calculateLength(normalVector)
			if(normalVectorLength > this.cueBall.diameter ){				
				return
			}
			const someRandomTestNumberToAvoidOverlapping = 4			
			const mtd = this.multiplyVector(normalVector, (this.cueBall.diameter - normalVectorLength) / normalVectorLength)
			componentA.position.x += mtd.x * 0.5
			componentA.position.y += mtd.y * 0.5
			componentB.position.x -= mtd.x * 0.5
			componentB.position.y -= mtd.y * 0.5
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
		},
		isMoving(component:IPoolComponent){
			if(!component)
			return false
			return component.velocity.x !== 0 || component.velocity.y !==0
		},
		checkAndHandleTableCollision(ball:IBall){		
			if(this.isBallInMiddleArea(ball)){			
				return
			}else if(this.isTableTopBoundry(ball) ){				
				ball.velocity.y = Math.abs(ball.velocity.y)
			}else if(this.isTableBottomBoundry(ball)){			
				ball.velocity.y = -Math.abs(ball.velocity.y)
			}else if(this.isTableLeftBoundry(ball) ){				
				ball.velocity.x = Math.abs(ball.velocity.x)
			}else if(this.isTableRightBoundry(ball) ){			
				ball.velocity.x = -Math.abs(ball.velocity.x)
			}else if(this.checkAndHandlePocketPathwayCollisions(ball)){			
			
			}else{
				const pocket:IPocket = this.checkIfGoesToPocket(ball)
				if(pocket){
					this.putBallInMiddleOfPocket(ball, pocket)
					setTimeout(() => {
						this.movePocketedBallToEdgeOfTable(ball)
						this.draw()
					}, 1500)
					
					return 
				}
			}
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
					ball.velocity = reflectionVector
					return true
				} else if(this.isPathwayBorderCollision(pocket.pathwayLeft, ball, i)){
					let reflectionVector = this.calculateBallVelocityOnPathwayBorderCollision(pocket.pathwayLeft, ball)
					ball.velocity = reflectionVector
					return true
				}				
			}
			return false
		},
		//Pathway to pocket means a separate area in front of the pocket which has two sides thus different angles if ball hits them
		calculateBallVelocityOnPathwayBorderCollision(pathway:IPathWayBorder, ball:IBall){
			// https://stackoverflow.com/questions/61272597/calculate-the-bouncing-angle-for-a-ball-point
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
				return false
			}
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
			for (let i = 0; i< this.balls.length; i++){				
				let comp:IPoolComponent = this.balls[i]
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
			this.cue.angle = tempAngle
			this.cue.image.canvasRotationAngle = tempAngle				
			this.t(this.sp, 1000, this.cue, this.cueBall, this.canvas)			
		},
		updateCueForce(){
			this.cue.force += 10		
			this.cue.image.canvasDestination.x  -= 5
			this.draw()
		
			if(this.cue.force >= 250){
				this.stopCueInterval()
				this.stopReducerInterval()			
				this.poolTable.mouseEnabled = false
				this.sendTurn(this.cue, this.cueBall, this.canvas)
			}
		},
		updatePointerLine(event:MouseEvent){
			if(!event){			
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
			if(i===1 || i===8 || i==13){				
				return 0
			}
			else if( i===3 || i===15){				
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
			}else if(i===5){
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
		},

		repaintNames(){
			this.renderingContext.font = "bolder 16px Arial";
			this.renderingContext.fillStyle = "black"
			this.renderingContext.fillText(this.theTable.playerA.name, this.canvas.width * 0.10, 20)
			this.renderingContext.fillText(this.theTable.playerB.name, this.canvas.width * 0.55, 20)
		},		
		repaintPocketSelection(selection:number){
			this.renderingContext.fillStyle = 'blue';
			this.renderingContext.lineWidth = 1
			this.renderingContext.beginPath()
			this.renderingContext.arc(this.poolTable.pockets[selection].center.x, this.poolTable.pockets[selection].center.y, this.poolTable.pockets[selection].radius, 0, 2 * Math.PI)
			this.renderingContext.stroke()
			this.renderingContext.closePath()
			this.renderingContext.fill()	
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
 -->