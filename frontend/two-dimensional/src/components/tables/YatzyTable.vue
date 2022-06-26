<template>
    <div class="row">
        <div class="col">
            <button v-if="true" :disabled="false" @click="" type="button"
                class="btn btn-primary w-30 float-xs-start float-sm-end">
                Resign
            </button>
            <button v-if="true" @click="" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
                Rematch
            </button>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-12 col-sm-4">
            <label class="selectora" for="notificationSound" data-bs-toggle="tooltip" data-bs-placement="top"
                title="Beeps last 14 seconds in turn if checked">
                <i class="bi bi-music-note"></i>
            </label>
            <input type="checkbox" id="notificationSound" v-model="gameOptions.notificationSound">
        </div>
    </div>
    <div class="row">

        <div class="col-xs-12 col-sm-4">           
            <span v-if="yatzyTable.playerInTurn?.name === userName" class="text-success"> It's your turn
                {{ userName }} time:{{ yatzyTable.secondsLeft }}  </span>
            <span v-else-if="yatzyTable?.playerInTurn === null" class="text-success"> Game ended </span>
            <div v-else class="text-danger"> In turn: {{ yatzyTable.playerInTurn?.name }} time:{{ yatzyTable.secondsLeft }}</div>
        </div>
    </div>
    <div>
        <canvas id="canvas" width="1200" height="600" style="border:1px solid"></canvas>
    </div>
   <!-- <chat :id="yatzyTable.tableId"> </chat>  -->
</template>

<script setup lang="ts">

import { IPlayer, IUser } from '@/interfaces/interfaces';
import { IScoreCard, IYatzyComponent, IYatzyTable } from '@/interfaces/yatzy';
import { ref, computed, onMounted, onBeforeMount, onUnmounted, watch, ComputedRef, } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { IGameOptions, Image, IVector2, IGameCanvas, FONT_SIZE, FONT, IChatMessage } from "../../interfaces/commontypes"
import { IYatzyPlayer, IYatzyMessage, IDice, ISection, IYatzySnapshot, IYatzyAction, IYatzyActionQueue, HandType, IHand, IScoreCardRow } from "../../interfaces/yatzy"
import Chat from "../Chat.vue"
const CANVAS_ROWS = 22
const router = useRouter()
let gameSnapshot: IYatzySnapshot = undefined
const store = useStore()
const gameOptions = ref<IGameOptions>({ notificationSound: false })


onMounted(() => {
    restartPlayersTimeInterval(yatzyTable.value.secondsLeft)
    let canvasElement = <HTMLCanvasElement>document.getElementById("canvas")
    yatzyTable.value.canvas = <IGameCanvas>{ element: canvasElement, animating: false, ctx: canvasElement.getContext("2d") }
    yatzyTable.value.canvas.ctx.imageSmoothingEnabled = true
    yatzyTable.value.scoreCardRows = initScoreCardRows()
    if (isMyTurn) {
        yatzyTable.value.canvas.enabled = true
    }
    attachListeners()
    resizeDocument()
    repaintYatzyTable()
    yatzyTable.value.secondsLeft
})

const scoreCardSection = (): ISection => {

    const canvasWidth = yatzyTable.value.canvas.element.width
    const start: IVector2 = { x: 0, y: 0 }
    let end: IVector2 = undefined
    if (canvasWidth >= 1200 || canvasWidth >= 1000 && yatzyTable.value.players.length <= 3) {

        let x = canvasWidth * 0.95 / 2
        let y = x
        //     yatzyTable.value.canvas.element.width = x
        //     yatzyTable.value.canvas.element.height = y
        end = { x: x, y: y }
        // yatzyTable.value.image.canvasDimension = {x:x, y:y}
        return { start, end }
    }
    if (canvasWidth >= 1000 && yatzyTable.value.players.length === 4) {

        let x = screen.width * 0.95
        let y = screen.height

        //yatzyTable.value.image.canvasDimension = {x:x, y:y}
        end = { x: x, y: y }
        return { start, end }
    }
    if (canvasWidth >= 768 && yatzyTable.value.players.length <= 3) {

        let x = screen.width * 0.99 / 2
        let y = x

        // yatzyTable.value.image.canvasDimension = {x:x, y:y}
        end = { x: x, y: y }
        return { start, end }
    }
    if (canvasWidth >= 768 && yatzyTable.value.players.length === 4) {

        let x = screen.width * 0.90
        let y = screen.height

        //  yatzyTable.value.image.canvasDimension = {x:x, y:y}
        end = { x: x, y: y }
        return { start, end }
    }

    const x = canvasWidth
    const y = yatzyTable.value.canvas.element.height * 0.75

    end = { x: x, y: screen.height * 0.75 }
    // yatzyTable.value.image.canvasDimension = {x:x, y:y}
    return { start, end }

}
const scoreCardHeight = () => {
    const s = scoreCardSection()
    return s.end.y - s.start.y
}
const initScoreCardRows = (): IScoreCardRow[] => {
    let rows: IScoreCardRow[] = []
    let initialRowHeight = scoreCardHeight() / CANVAS_ROWS
    if (yatzyTable.value.canvas.element.width < 768) {
        initialRowHeight = yatzyTable.value.canvas.element.height * 0.5 / CANVAS_ROWS
    }
    //Wrapper object with section and height..?
    rows.push(createScoreCardRow("Ones [5]", 4, HandType.ONES, initialRowHeight))
    rows.push(createScoreCardRow("Twos [10]", 5, HandType.TWOS, initialRowHeight))
    rows.push(createScoreCardRow("Threes [15]", 6, HandType.THREES, initialRowHeight))
    rows.push(createScoreCardRow("Fours [20]", 7, HandType.FOURS, initialRowHeight))
    rows.push(createScoreCardRow("Fives [25]", 8, HandType.FIVES, initialRowHeight))
    rows.push(createScoreCardRow("Sixes[30]", 9, HandType.SIXES, initialRowHeight))
    rows.push(createScoreCardRow("Subtotal [105]", 10, HandType.SUBTOTAL, initialRowHeight))
    rows.push(createScoreCardRow("Bonus [50] (upper > 63)", 11, HandType.BONUS, initialRowHeight))
    rows.push(createScoreCardRow("Pair [12]", 12, HandType.PAIR, initialRowHeight))
    rows.push(createScoreCardRow("Two pairs [22]", 13, HandType.TWO_PAIR, initialRowHeight))
    rows.push(createScoreCardRow("Trips [18]", 14, HandType.TRIPS, initialRowHeight))
    rows.push(createScoreCardRow("Full house [28]", 15, HandType.FULL_HOUSE, initialRowHeight))
    rows.push(createScoreCardRow("Low straight (1-5) [15]", 16, HandType.SMALL_STRAIGHT, initialRowHeight))
    rows.push(createScoreCardRow("High straight (2-6) [20]", 17, HandType.LARGE_STRAIGHT, initialRowHeight))
    rows.push(createScoreCardRow("Quads [24]", 18, HandType.QUADS, initialRowHeight))
    rows.push(createScoreCardRow("Chance [30]", 19, HandType.CHANCE, initialRowHeight))
    rows.push(createScoreCardRow("Yatzy [50]", 20, HandType.YATZY, initialRowHeight))
    rows.push(createScoreCardRow("Grand Total [374]", 21, HandType.TOTAL, initialRowHeight))
    return rows
}
const initTable = (): IYatzyTable => {
    console.log("initTable:")
    let initialTable: IYatzyTable = store.getters.yatzyTable
    const bgSize: IVector2 = { x: 1600, y: 1600 }
    const tableImage = <Image>{
        image: <HTMLImageElement>document.getElementById("yatzybg"),
        canvasDimension: bgSize,
        canvasDestination: <IVector2>{ x: 0, y: 0 },
        realDimension: { x: 1024, y: 1024 },
        canvasRotationAngle: 0,
        visible: true
    }
    initialTable.image = tableImage
    const players: IYatzyPlayer[] = initialTable.players
    initialTable.players.forEach(player => {
        player.scoreCard = { bonus: undefined, subTotal: 0, total: 0, hands: [], lastAdded: null }
    })

    for (let i = 1; i < 6; i++) {
        const diceImage = <Image>{
            image: <HTMLImageElement>document.getElementById("dice" + i),
            canvasDimension: { x: 80, y: 80 },
            realDimension: { x: 1024, y: 1024 },
            canvasRotationAngle: 0,
            canvasDestination: <IVector2>{ x: 0, y: 0 },
            visible: true
        }
        let dice: IDice = initialTable.dices[i - 1]
        dice.image = diceImage
    }

    initialTable.players = players
    const playButtonImage = <Image>{
        image: <HTMLImageElement>document.getElementById("emptyButton"),
        canvasDimension: { x: 100, y: 50 },
        realDimension: { x: 727, y: 279 },
        canvasRotationAngle: 0,
        canvasDestination: <IVector2>{ x: 0, y: 0 },
        visible: true
    }

    initialTable.playButton = { image: playButtonImage, position: { x: 650, y: 300 } }
    initialTable.position = <IVector2>{ x: 0, y: 0 }

    return initialTable
}

const yatzyTable = ref<IYatzyTable>(initTable())

const actionQueue = ref<IYatzyActionQueue>({ actions: [], blocked: false })
const userName = computed<string>(() => store.getters.user?.name)
const user = computed<IUser>(() => store.getters.user)
const queueLength = computed<number>(() => actionQueue.value.actions.length)
const playerInTurn = computed<IYatzyPlayer>(() => <IYatzyPlayer>yatzyTable.value.playerInTurn)
const isAllowedToSelectDice = computed<boolean>(() => playerInTurn.value.rollsLeft > 0 && playerInTurn.value.rollsLeft < 3 && yatzyTable.value.canvas.animating === false)
const isAllowedToSelectHand = computed<boolean>(() => isMyTurn.value === true && playerInTurn.value.rollsLeft < 3 && yatzyTable.value.canvas.animating === false)
const isMyTurn = computed<boolean>(() => yatzyTable.value?.playerInTurn?.name === userName.value)
const isRollButtonVisible = computed<boolean>(() => isMyTurn.value && playerInTurn.value.rollsLeft > 0 && yatzyTable.value.canvas?.animating === false && isAllDicesSelected.value === false)
const isAllDicesSelected = computed<boolean>(() => yatzyTable.value.dices.filter(dice => dice.selected).length === 5)
const isMouseEnabled = computed<boolean>(() =>
    yatzyTable.value.canvas.enabled && isMyTurn.value === true
)

watch(() => queueLength, (newVal) => {
    if (newVal.value > 0) {
        consumeActions()
    }
}, { deep: true })


const consumeActions = () => {
    if (actionQueue.value.blocked || actionQueue.value.actions.length === 0) {
        console.log("actionQueue not consuming " + actionQueue.value.actions.length)
        return
    }
    actionQueue.value.blocked = true
    const action: IYatzyAction = actionQueue.value.actions.splice(0, 1)[0]   
    expectingServerResponse = false
    if (action.type === "yatzyRollDices") {        
        animateDices(yatzyTable.value.dices, action.payload.table.dices).then(() => {
            const player = <IYatzyPlayer>yatzyTable.value.playerInTurn
            playerInTurn.value.rollsLeft = action.payload.table.playerInTurn.rollsLeft
            setupDices(yatzyTable.value.dices, action.payload.table.dices, player.rollsLeft === 0)
            drawAll()
            unblockQueue()
            console.log("animations done")
        })
    } else if (action.type === "yatzySelectHand") {        
        updateScoreCard(action.payload)
        repaintScoreCard()
        if(action.payload.yatzy.gameOver){
            handleGameOver()
        } else {
            unblockQueue()
        }
    } else if(action.type === "changeTurn") {
        store.dispatch("changeTurn", action.payload.table.playerInTurn).then(() => {
            yatzyTable.value.dices.forEach(dice => {
                dice.selected = false
            })
            unblockQueue()
            document.body.style.cursor = "default"
            yatzyTable.value.playerInTurn = action.payload.table.playerInTurn           
            repaintYatzyTable()
        })
       } else if(action.type === "timeout"){
            let yatzyPlayer = <IYatzyPlayer> yatzyTable.value.players.find(player => player.name === action.payload.table.timedOutPlayerName)
            yatzyPlayer.enabled = false         
            if(userName.value === yatzyPlayer.name){
                yatzyTable.value.canvas.enabled = false
            }
            repaintScoreCard()
            if(action.payload.yatzy.gameOver){
                handleGameOver()
            } else {
                unblockQueue()
            } 
        }
}
const handleGameOver = () => {
    stopPlayerTimeInterval()
    yatzyTable.value.playerInTurn = null
	actionQueue.value.blocked = true
	yatzyTable.value.canvas.enabled = false
	const message:IChatMessage = {from:"System",text: "Game ended"}					
    store.dispatch("chat", message)
    drawAll()
}
let playerTimeInterval = null
//let secondsLeft = null
const stopPlayerTimeInterval = () =>  {
    playerTimeInterval = clearInterval(playerTimeInterval)
}
const restartPlayersTimeInterval = (seconds:number) => {
    console.log("restartInterval "+seconds)
    if(playerTimeInterval){
		stopPlayerTimeInterval()
    }
	yatzyTable.value.secondsLeft = seconds		
	playerTimeInterval = setInterval(() => {
        console.log("seonds left:"+yatzyTable.value.secondsLeft )
		yatzyTable.value.secondsLeft --
        if(yatzyTable.value.secondsLeft  <= 0 || !yatzyTable.value.secondsLeft )
            stopPlayerTimeInterval()
	}, 1000)
}
const updateScoreCard = (payload:any) => {
    let yatzyPlayer: IYatzyPlayer = yatzyTable.value.players.find(player => player.name === payload.yatzy.whoPlayed)
    yatzyPlayer.scoreCard.total = payload.yatzy.scoreCard.total
    yatzyPlayer.scoreCard.bonus = payload.yatzy.scoreCard.bonus
    yatzyPlayer.scoreCard.subTotal = payload.yatzy.scoreCard.subTotal
    yatzyPlayer.scoreCard.lastAdded = payload.yatzy.scoreCard.lastAdded
    yatzyPlayer.scoreCard.hands.push({ handType: payload.yatzy.scoreCard.lastAdded.handType, typeNumber: payload.yatzy.scoreCard.lastAdded.typeNumber, value: payload.yatzy.scoreCard.lastAdded.value })
}
const unblockQueue = (timeout: number = 0) => {
    expectingServerResponse = false
    actionQueue.value.blocked = false
    setTimeout(() => {
        consumeActions()
    }, timeout)
}

onUnmounted(() => {
    console.log("onUnmounted")
    const obj = { title: "LEAVE_TABLE", message: yatzyTable.value.tableId }
    store.getters.user.webSocket?.send(JSON.stringify(obj))
    store.dispatch("clearTable")
})

const setCanvasPositionToLeft = () => {
    yatzyTable.value.canvas.element.style.position = 'absolute'
    yatzyTable.value.canvas.element.style.left = '0px'
}

const resetCanvasPosition = () => {
    yatzyTable.value.canvas.element.style.position = 'relative '
    yatzyTable.value.canvas.element.style.left = ''
}

const resizeDocument = () => {
    //Resizes canvas size and sets position
    console.log("resizeEvent screenWidth" + screen.width)
    setTableLayout()
    drawAll()
}

let verticalLayout: boolean = true

const setTableLayout = (): boolean => {
    verticalLayout = true
    if (window.innerWidth >= 1200) {
        resetCanvasPosition()
        yatzyTable.value.canvas.element.width = 1100
        yatzyTable.value.canvas.element.height = window.innerHeight
    } else if (window.innerWidth >= 1000 && yatzyTable.value.players.length > 2) {
        yatzyTable.value.canvas.element.width = window.innerWidth * 0.9
        yatzyTable.value.canvas.element.height = 1000
    }
    else if (window.innerWidth >= 940) {
        if (yatzyTable.value.playerAmount >= 3) {
            verticalLayout = false
        }
        setCanvasPositionToLeft()
        yatzyTable.value.canvas.element.width = window.innerWidth
        yatzyTable.value.canvas.element.height = window.innerHeight
    }
    else {
        verticalLayout = false
        setCanvasPositionToLeft()
        yatzyTable.value.canvas.element.width = screen.width
        yatzyTable.value.canvas.element.height = window.innerHeight
    }
    yatzyTable.value.scoreCardRows = initScoreCardRows()
    const size = diceSize()
    yatzyTable.value.dices.forEach((dice) => {
        dice.image.canvasDimension.x = size.x
        dice.image.canvasDimension.y = size.y
    })
    return verticalLayout
}

let expectingServerResponse: boolean = false
const sendRollRequest = () => {
    highlightedRow = null 
    if (isRollButtonVisible.value === false || yatzyTable.value.canvas.animating === true || expectingServerResponse === true) {
        return
    }
    expectingServerResponse = true
    let rollTheseDices: IDice[] = []
    for (let i = 0; i < yatzyTable.value.dices.length; i++) {
        const { diceId, selected, number, position: position }: IDice = yatzyTable.value.dices[i]
        if (!selected)
            rollTheseDices.push({ diceId: diceId, selected: selected, number: number, position: position })
    }
    const obj = { title: "YATZY_ROLL_DICES", message: yatzyTable.value.tableId, yatzy: { dices: rollTheseDices } }
    console.log("*** Sending dices " + JSON.stringify(obj))
    user.value.webSocket.send(JSON.stringify(obj))
}

const sendHandSelection = (cursorPoint: IVector2) => {
    if (isMyTurn.value !== true || playerInTurn.value.rollsLeft === 3 || yatzyTable.value.canvas.animating === true || highlightedRow === null || expectingServerResponse === true) {
        return
    }
    //expectingServerResponse = true
    const obj = { title: "YATZY_SELECT_HAND", message: yatzyTable.value.tableId, yatzy: { handVal: highlightedRow.handType } }
    console.log("*** Sending selection " + JSON.stringify(obj))
    user.value.webSocket.send(JSON.stringify(obj))
    highlightedRow = null
}

const attachListeners = () => {
    window.addEventListener('resize', resizeDocument)

    const canvas = document.getElementById("canvas")
    canvas.addEventListener('pointerover', (event) => {
        if (!isMouseEnabled.value) {
            return
        }
    })
    canvas.addEventListener('pointerdown', (event) => {
        if (!isMouseEnabled.value) {
            return
        }
        const cursorPoint: IVector2 = { x: event.offsetX, y: event.offsetY }
        console.log('Pointer down event')
        const dice: IDice = getDice(cursorPoint)
        if (dice && isAllowedToSelectDice.value === true) {
            dice.selected ? dice.selected = false : dice.selected = true
        } else if (isPointOnImage(cursorPoint, yatzyTable.value.playButton.image)) {
            document.body.style.cursor = "default"
            sendRollRequest()
        } else if (isAllowedToSelectHand.value === true && isPointOnSection(cursorPoint, scoreCardSection())) {
            sendHandSelection(cursorPoint)
        }

        repaintYatzyTable()
    })
    canvas.addEventListener('pointermove', (event) => {
       
        const cursorPoint = { x: event.offsetX, y: event.offsetY }
        if (!isMouseEnabled.value) {
            return
        }
        document.body.style.cursor = "default"
        if (isAllowedToSelectDice.value === true && isPointOnSection(cursorPoint, diceSection())) {
            const dice: IDice = getDice(cursorPoint)
            if (dice) {
                document.body.style.cursor = "pointer"
            }
        } else if (isAllowedToSelectHand.value === true && isPointOnSection(cursorPoint, scoreCardSection())) {    
            document.body.style.cursor = "pointer"
            highlightedRow = null
            getScoreCardRow(cursorPoint)
            drawAll() //drawScoreCard=
        } else if (isPointOnSection(cursorPoint, buttonSection())) {
            if (isRollButtonVisible.value === true && isPointOnImage(cursorPoint, yatzyTable.value.playButton.image)) {
                document.body.style.cursor = "pointer"
                //   repaintYatzyTable()
            }
        }
    })
    document.addEventListener("visibilitychange", onVisibilityChange)

    canvas.addEventListener("contextmenu", (e) => e.preventDefault())
}

const getDice = (cursorPoint: IVector2) => {
    return yatzyTable.value.dices.filter(dice => getDiceOnCursor(dice, cursorPoint))[0]
}
const isDocumentVisible = () => {
    return document.visibilityState === 'visible'
}
const createTableFromSnapShot = () => {
    if(!gameSnapshot?.table){
        return
    }
    setupDices(yatzyTable.value.dices, gameSnapshot.table.dices, false)
    //Creates all hands from scratch -> although lastAdded, bonus, subTotal and Total are the only ones with re-calculations requirements
    //Can be used for watcher which need to build all from scratch
    gameSnapshot.table.players.forEach(snapshotPlayer => {     
        const tablePlayer:IYatzyPlayer = yatzyTable.value.players.find(player => player.name === snapshotPlayer.name)
        const snapshotPlayerHands:IHand [] = snapshotPlayer.scoreCard.hands
        tablePlayer.scoreCard.hands = []
        for (const [key, value] of Object.entries(snapshotPlayerHands)) {          
            tablePlayer.scoreCard.hands.push(value)
        }
        tablePlayer.scoreCard.bonus = snapshotPlayer.scoreCard.bonus
        tablePlayer.scoreCard.total = snapshotPlayer.scoreCard.total
        tablePlayer.scoreCard.subTotal = snapshotPlayer.scoreCard.subTotal
        tablePlayer.enabled = snapshotPlayer.enabled    
    })
    actionQueue.value.actions = []
    const action = {payload: gameSnapshot}
    if(gameSnapshot.yatzy.gameOver){
        return handleGameOver()
    }
    initNewTurnIfRequired(action)
}
const onVisibilityChange = () => { 
    console.log("onVisiblityChange "+isDocumentVisible())
    if (isDocumentVisible()) {	  
		createTableFromSnapShot()
		drawAll()
        actionQueue.value.blocked = false	
    } else {
		actionQueue.value.blocked = true	
	}
}
let highlightedRow: IScoreCardRow = null

const getScoreCardRow = (cursorPoint: IVector2) => {
    const yy = cursorPoint.y - scoreCardSection().start.y
    let initialRowHeight = scoreCardHeight() / CANVAS_ROWS
    let nth: number = Math.floor(yy / (initialRowHeight))
    const row: IScoreCardRow = yatzyTable.value.scoreCardRows[nth - 4] 
    if(!row || !isHighlightableRow(row)){       
        document.body.style.cursor = "default"
        return
    }  
    if(playerInTurn.value.scoreCard.hands.length === undefined){
        highlightedRow = row
        return
    }  
    let index =  playerInTurn.value.scoreCard.hands.findIndex(hand => {    
        hand.handType === row.handType
    })
    index === -1? highlightedRow = row : ""
}
const isHighlightableRow = (row:IScoreCardRow) => {
    if( row.handType === HandType.BONUS || row.handType === HandType.SUBTOTAL || row.handType === HandType.TOTAL){
        return false
    }
    const scoreCard:IScoreCard = yatzyTable.value.players.find(player => player.name === userName.value).scoreCard
    const hand:IHand = scoreCard.hands.find(hand => hand.handType.toString() === HandType[row.handType].toString())   
    return hand ? false : true
}

const getDiceOnCursor = (dice: IDice, cursorPoint: IVector2) => {
    const size = diceSize()
    if (cursorPoint.x < dice.position.x) {
        //  console.log("failed1")
        return false
    }
    if (cursorPoint.x > dice.position.x + size.x) {
        //  console.log("failed22")
        return false
    }
    if (cursorPoint.y < dice.position.y) {
        //  console.log("failed3")
        return false
    }
    if (cursorPoint.y > dice.position.y + size.y) {
        return false
    }
    return true
}

const isPointOnImage = (point: IVector2, image: Image) => {

    if (point.x < image.canvasDestination.x || point.x > image.canvasDestination.x + image.canvasDimension.x) {
        return false
    }
    if (point.y < image.canvasDestination.y || point.y > image.canvasDestination.y + image.canvasDimension.y) {
        return false
    }
    return true
}

const isPointOnSection = (point: IVector2, section: ISection) => {
    if (point.x < section.start.x || point.x > section.end.x) {
        return false;
    }
    if (point.y < section.start.y || point.y > section.end.y) {
        return false;
    }
    return true
}

const unsubscribe = store.subscribe((mutation, state) => {
    if(mutation.type === "changeTurn"){  
       console.log("changePlayer mutation")
    }
    if (mutation.type === "rematch") {
        console.log("rematch sub")
    }
    if (mutation.type === "resign") {
        console.log("resign sub")
    }
})

const unsubscribeAction = store.subscribeAction((action, state) => {
    if (action.type === "changeTurn" || action.type === "chat") {
       // yatzyTable.value.playerInTurn = action.payload.nextTurnPlayer
        return
    }
    restartPlayersTimeInterval(action.payload.table.secondsLeft)
    gameSnapshot = action.payload
    actionQueue.value.actions.splice(actionQueue.value.actions.length, 0, action)    
    initNewTurnIfRequired(action)
})

const initNewTurnIfRequired = (action) => {    
    if(action.payload.yatzy.gameOver){
        return
    }  
    //One person can play to the end if others timeouts or leaves
    if (action.payload.table.playerInTurn.name !== yatzyTable.value.playerInTurn.name || yatzyTable.value.players.filter(player => player.enabled === true).length === 1 ) {      
        let changeTurnAction = { type:"changeTurn", payload:action.payload }
        actionQueue.value.actions.splice(actionQueue.value.actions.length, 0, changeTurnAction)
    }
}

const repaintComponent = (component: IYatzyComponent) => {
    if (!component) {
        return
    }
    const ctx = yatzyTable.value.canvas.ctx
    if (!component.image.visible) {
        return
    }
    if (component.image.canvasRotationAngle) {
        ctx.rotate(component.image.canvasRotationAngle)
    }
    if (!component.image.canvasDestination) {
        component.image.canvasDestination = <IVector2>{ x: 0, y: 0 }
    }
    ctx.drawImage(component.image.image, 0, 0, component.image.realDimension.x, component.image.realDimension.y, component.image.canvasDestination.x, component.image.canvasDestination.y, component.image.canvasDimension.x, component.image.canvasDimension.y)
}

const repaintDices = () => {
    const section = diceSection()
    for (let i = 0; i < 5; i++) {
        const dice = yatzyTable.value.dices[i]
        const size: IVector2 = diceSize()
        if (dice.selected) {
            //position down size of one dice
            dice.position = { x: section.start.x + i * size.x + 60, y: section.start.y + size.y }
        } else if (playerInTurn?.value?.rollsLeft === 0 && dice.selected === false) {
            dice.position = { x: section.start.x + i * size.x + 60, y: section.start.y + (dice.position.y - section.start.y) }
        } else {
            //basic position
            dice.position = { x: section.start.x + i * size.x + 60, y: section.start.y }
        }
        dice.image.canvasDestination = dice.position
        dice.image.canvasDimension.x = size.x
        dice.image.canvasDimension.x = size.y
        repaintComponent(dice)
    }
}

const repaintButtons = () => {
      
    if (isRollButtonVisible.value === true) {
        const button = yatzyTable.value.playButton
        const section: ISection = buttonSection()
        button.position.x = section.end.x - button.image.canvasDimension.x
        button.position.y = section.end.y - button.image.canvasDimension.y
        button.image.canvasDestination = button.position
        const ctx = yatzyTable.value.canvas.ctx
        ctx.beginPath()
        const count = yatzyTable.value.dices.filter(dice => !dice.selected).length
        ctx.font = "bold " + FONT_SIZE.LARGEST + " Arial"
        ctx.font = "18px bolder Arial"
        ctx.fillText("Roll " + count, button.position.x + 20, button.position.y + 30)
        ctx.closePath()
        repaintComponent(button)
    }
}

//i18n !?
const rollDicesText = "Click the roll button"
const selectHandText = "Select a hand"
const selectOrRollText = "Select a hand, lock dices or roll unlocked dices"
let nowInTurn = playerInTurn.value.name + " is now in turn"
let itYourTurn = "It's your turn " + userName.value
const waitingTurnText = "Waiting your turn"
const youHaveTimedOut = "You timed out"

const optionsText = (): string => {
    if (isMyTurn.value === false) {
        return waitingTurnText
    }
    if (playerInTurn.value.rollsLeft === 3) {
        return rollDicesText
    }
    if (playerInTurn.value.rollsLeft === 0) {
        return selectHandText
    }
    return selectOrRollText
}
const repaintInfoTexts = () => {
    const x = diceSection().start.x + 75
    const turnInfoY = diceSection().start.y - 30
    const y = diceSection().start.y - 10
    const ctx = yatzyTable.value.canvas.ctx
    ctx.beginPath()
    ctx.font = "18px bolder Arial"
    if(gameSnapshot?.yatzy?.gameOver){
        ctx.fillText("GAME OVER", x, turnInfoY)
        ctx.closePath()
        return
    }
    isMyTurn.value === true ? ctx.fillStyle = "green" : ctx.fillStyle = "red"
    ctx.fillText(isMyTurn.value === false ? "In turn "+playerInTurn.value.name : itYourTurn, x, turnInfoY)
    ctx.fillStyle = "black"
    ctx.fillText(optionsText() + " (left = " + playerInTurn.value.rollsLeft + ")", x, y)
    ctx.closePath()
}


const diceSection = (): ISection => {

    const dSize: IVector2 = diceSize()
    const canvasWidth = yatzyTable.value.canvas.element.width
    if (verticalLayout && screen.width >= 1000) {

        const y = yatzyTable.value.canvas.element.height / 10
        const x = yatzyTable.value.canvas.element.width * 0.50
        return { start: { x: x, y: y }, end: { x: x + 6 * dSize.x, y: y + (dSize.y * 2) } }
    }
    if (verticalLayout && window.innerWidth < 1000) {

        const y = yatzyTable.value.canvas.element.height / 10
        const x = yatzyTable.value.canvas.element.width * 0.50
        return { start: { x: x, y: y }, end: { x: x + 6 * dSize.x, y: y + (dSize.y * 2) } }
    }

    return { start: { x: 0, y: yatzyTable.value.canvas.element.height * 0.75 }, end: { x: 20 + 6 * dSize.x, y: yatzyTable.value.canvas.element.height * 0.95 } }
}

const buttonSection = (): ISection => {
    //button section comes under dice section
    const dices: ISection = diceSection()
    const dSize = diceSize()
    if (window.innerWidth >= 500 && verticalLayout) {
        const buttonSectionStart: IVector2 = { x: dices.start.x, y: dices.start.y + dSize.y }
        const buttonSectionEnd: IVector2 = { x: dices.end.x, y: dices.end.y + 2.5 * dSize.y }
        return { start: buttonSectionStart, end: buttonSectionEnd }
    }
    if (window.innerWidth >= 670 && !verticalLayout) {
        const buttonSectionStart: IVector2 = { x: dices.end.x, y: dices.start.y }
        const buttonSectionEnd: IVector2 = { x: dices.end.x + 2 * dSize.x, y: dices.start.y }
        return { start: buttonSectionStart, end: buttonSectionEnd }
    }
    if (!verticalLayout) {
        const buttonSectionStart: IVector2 = { x: dices.start.x, y: dices.start.y - 1.5 * dSize.y }
        const buttonSectionEnd: IVector2 = { x: dices.end.x, y: dices.start.y - dSize.y }
        return { start: buttonSectionStart, end: buttonSectionEnd }
    }

}

const diceSize = (): IVector2 => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    if (verticalLayout && screen.width >= 1000) {
        return { x: 85, y: 85 }
    }
    if (verticalLayout && window.innerWidth < 1000) {
        return { x: 70, y: 70 }
    }
    return window.innerWidth < 500 ? { x: 60, y: 60 } : { x: 80, y: 80 }
}
let animationRun = 0
const delay = millis => new Promise(resolve => setTimeout(resolve, millis))

const drawAll = () => {
    console.log("DRAW all")
    requestAnimationFrame(repaintYatzyTable)
}
const animateDices = async (tableDices: IDice[], resultDices: IDice[]) => {
    yatzyTable.value.canvas.animating = true
    if (animationRun !== 0) {
        randomizeDices(yatzyTable.value.dices)
        requestAnimationFrame(repaintDices)
    } else {
        randomizeDices(yatzyTable.value.dices)
       
        drawAll()
    }
    animationRun++
    if (animationRun < 10) {
        await delay(250)
        await animateDices(tableDices, resultDices)
    } else {
        animationRun = 0
        yatzyTable.value.canvas.animating = false
    }
}

const setupDices = async (tableDices: IDice[], resultDices: IDice[], animate: boolean = false) => {
    resultDices.forEach(resultDice => {
        const tableDice: IDice = tableDices.find(tableDice => tableDice.diceId === resultDice.diceId)
        setupDice(tableDice, resultDice.number)
    })
    if (animate === false) {
        return
    }
    let counter = 0
    const finalDices: IDice[] = tableDices.filter(tableDice => !tableDice.selected)
    const size: IVector2 = diceSize()
    yatzyTable.value.canvas.animating = true
    while (counter < 16) {
        for (let i = 0; i < finalDices.length; i++) {
            let finalDice = finalDices[i]
            finalDice.position.y += size.y / 16
        }
        drawAll()
        await delay(50)
        counter++
    }
    yatzyTable.value.canvas.animating = false
}

const setupDice = (dice: IDice, number: number) => {
    dice.number = number
    dice.image.image = <HTMLImageElement>document.getElementById("dice" + number)
    
}

const setRandomNumber = (dice: IDice) => {
    if (!dice.selected) {
        setupDice(dice, getRandomNumber())
    }
}

const randomizeDices = (dices: IDice[]) => {
    dices.forEach(dice => {
        setRandomNumber(dice)
    })
}

const getRandomNumber = (): number => {
    return Math.floor(Math.random() * 6 + 1);
}

const getPlayerHand = (handType: HandType, player: IYatzyPlayer): IHand => {
    let hand: IHand = player.scoreCard.hands.find(hand => hand.handType.toString() === HandType[handType].toString())
    if (hand) {      
        return hand
    }
    if (handType === HandType.BONUS) {
        hand = { handType: undefined, value: player.scoreCard.bonus, typeNumber: 16 }
    } else if (handType === HandType.SUBTOTAL) {
        hand = { handType: undefined, value: player.scoreCard.subTotal, typeNumber: 17 }
    } else if (handType === HandType.TOTAL) {
        hand = { handType: undefined, value: player.scoreCard.total, typeNumber: 18 }
    }
    return hand
    return null
}

const repaintYatzyTable = () => {
    const canvas = yatzyTable.value.canvas   
    const ctx = canvas.ctx
    ctx.clearRect(0, 0, canvas.element.width, canvas.element.height)
    repaintComponent(yatzyTable.value)
    repaintScoreCard()
    repaintDices()
    repaintButtons()
    repaintInfoTexts()
}

const magic: number = 120
const fillPlayerScore = (player:IYatzyPlayer, hand: IHand, startPoint: IVector2) => {
    const ctx = yatzyTable.value.canvas.ctx
    if (hand && hand.value > -1) {
        let handValue = hand.value > -1 ? hand.value.toString() : " - " 
        ctx.fillStyle = player.enabled? "black" : "gray"
        ctx.fillText(handValue, startPoint.x, startPoint.y)
    }
    ctx.fillStyle = "black"
}
const scorecardRowStart = 25
const scoreCardTextEnd = 200
const gapBetweenScoreCardRowTitleAndLine = 15

const drawScoreCardRow = (row: IScoreCardRow, rowHeight: number) => {
    const ctx = yatzyTable.value.canvas.ctx

    ctx.fillText(row.title, row.section.start.x, rowHeight * row.nth + gapBetweenScoreCardRowTitleAndLine)
    fillPlayersPointsOnRow(row, rowHeight)
    ctx.moveTo(row.section.start.x, row.section.start.y)
    ctx.lineTo(row.section.end.x, row.section.end.y)
    ctx.stroke()
    if (highlightedRow?.handType === row.handType) {

        ctx.strokeStyle = "#FF0000"
        ctx.lineWidth = 3
        ctx.strokeRect(row.section.start.x, rowHeight * row.nth, scoreCardSection().end.x, row.height)
    }
    ctx.lineWidth = 1
    ctx.strokeStyle = "#000000"
}
const fillPlayersPointsOnRow = (row: IScoreCardRow, rowHeight: number) => {
    for (let i = 0; i < yatzyTable.value.players.length; i++) {       
        const hand: IHand = getPlayerHand(row.handType, yatzyTable.value.players[i])
        const startPoint: IVector2 = { x: scoreCardTextEnd + 50 + magic * i, y: rowHeight * (row.nth + 1) -5 }
        fillPlayerScore(yatzyTable.value.players[i], hand, startPoint)
    }
}

const createScoreCardRow = (title: string, nthRow: number, handType: HandType, height: number): IScoreCardRow => {
    let sc: ISection = scoreCardSection()
    //  sc.start.y = sc.start.y * nthRow +6
    // sc.end.y = sc.end.y * nthRow +6
    const rowSection: ISection = { start: { x: scorecardRowStart, y: height * nthRow }, end: { x: sc.end.x, y: height * nthRow } }
    return { section: rowSection, title: title, nth: nthRow, handType: handType, height: height }
}

const repaintScoreCard = () => {
    const canvas = yatzyTable.value.canvas
    const cardSection = scoreCardSection()
    let rowHeight = scoreCardHeight() / CANVAS_ROWS    
    const width = canvas.element.width
    const ctx = canvas.ctx
    ctx.font = "bolder " + FONT_SIZE.LARGEST + " Arial"

    ctx.beginPath()
    ctx.fillText("Scorecards", 150, 35)
    ctx.font = "bold " + FONT_SIZE.DEFAULT + " Arial"
    if (canvas.element.width < 768) {
        rowHeight = canvas.element.height * 0.5 / CANVAS_ROWS
        ctx.font = FONT_SIZE.DEFAULT + " Arial"
    }
    yatzyTable.value.scoreCardRows.forEach(row => {
        drawScoreCardRow(row, rowHeight)
    })
    //Vertical lines
    for (let i = 0; i < yatzyTable.value.players.length; i++) {
        ctx.fillStyle = yatzyTable.value.players[i].enabled? "black" : "gray"
        ctx.fillText(yatzyTable.value.players[i].name, scoreCardTextEnd + 20 + i * magic, rowHeight * 3)
        ctx.moveTo(scoreCardTextEnd + i * magic, rowHeight * 3)
        ctx.lineTo(scoreCardTextEnd + i * magic, rowHeight * 21)
        ctx.stroke()
    }
    ctx.fillStyle = "black"
    ctx.closePath()
}

</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<!--
 * 	@author antsa-1 from GitHub 
 -->
<style scoped>
.hidden {
    visibility: hidden
}

.yatzyLeft {
    position: absolute;
    left: 0px
}
</style>
