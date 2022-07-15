<template>
    <div class="row">
        <div class="col">
            <button v-if="imPlaying()" :disabled="!isResignButtonEnabled" @click="resignButtonClick" type="button"
                class="btn btn-primary w-30 float-xs-start float-sm-end">
                Resign
            </button>
            <button v-if="imPlaying()" :disabled="!isRematchButtonEnabled" @click="rematch" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
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
            <span class="ps-4">
				<input class="" type="checkbox" id="animate" @click="onAnimationChange" v-model= "gameOptions.animations">
				<label class="" for="animate"> animate</label>
			</span>
        </div>
    </div>
    <div class="row">
        <div class="col-xs-12 col-sm-4">
            <span v-if="yatzyTable.playerInTurn?.name === userName" class="text-success"> It's your turn
                {{ userName }} time:{{ yatzyTable.secondsLeft }} </span>
            <span v-else-if="yatzyTable?.playerInTurn === null" class="text-success"> Game ended </span>
            <div v-else class="text-danger"> In turn: {{ yatzyTable.playerInTurn?.name }} time:{{ yatzyTable.secondsLeft}}</div>
        </div>
    </div>
    <div>
        <canvas id="canvas" width="1200" height="600" style="border:1px solid"></canvas>
    </div>
    <chat v-if="isChatVisible" :id="yatzyTable.tableId"> </chat>
</template>

<script setup lang="ts">

import { IPlayer, IUser } from '@/interfaces/interfaces';
import { IScoreCard, IYatzyComponent, IYatzyTable } from '@/interfaces/yatzy';
import { ref, computed, onMounted, onBeforeMount, onUnmounted, watch, ComputedRef, } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { leaveTable, rematch, resign } from '../composables/tableComposable'
import {Image, IVector2, IGameCanvas, FONT_SIZE, FONT, IChatMessage } from "../../interfaces/commontypes"
import { IYatzyPlayer, IYatzyMessage, IDice, ISection, IYatzySnapshot, IYatzyAction, IYatzyActionQueue, HandType, IHand, IScoreCardRow, IYatzyOptions } from "../../interfaces/yatzy"
import Chat from "../Chat.vue"
const CANVAS_ROWS = 22
const router = useRouter()
let gameSnapshot: IYatzySnapshot = null
const store = useStore()
const gameOptions = ref<IYatzyOptions>({ notificationSound: false, animations:true })
const props = defineProps(['watch'])


onMounted(() => {
    startCountdownTimer(yatzyTable.value.secondsLeft)
    if(yatzyTable.value.timeControlIndex < 2){
        gameOptions.value.animations = false
    }
    setupCanvas()
    if (props.watch === "1") {
        const tableSnapshot: IYatzyTable = <IYatzyTable>store.getters.theTable
        gameSnapshot = { table: tableSnapshot, yatzy: null }
        createTableFromSnapShot()
    } else {
        attachListeners()
    }
    resizeDocument()
    repaintYatzyTable()
})

const setupCanvas = () => {
    let canvasElement = <HTMLCanvasElement>document.getElementById("canvas")
    yatzyTable.value.canvas = <IGameCanvas>{ element: canvasElement, animating: false, ctx: canvasElement.getContext("2d") }
   // yatzyTable.value.canvas.element.width = window.innerWidth> 1200? 1195:window.innerWidth -10
   // yatzyTable.value.canvas.element.height = window.innerWidth> 1200? 1195:window.innerWidth -10
    yatzyTable.value.canvas.ctx.imageSmoothingEnabled = true
    yatzyTable.value.scoreCardRows = initScoreCardRows()
    
    buttonSection()
    if (isMyTurn.value) {
        yatzyTable.value.canvas.enabled = true
    }
}
const onAnimationChange = () =>{
    if(gameOptions.value.animations){ // v-model has not yet changed immediately after click
        yatzyTable.value.canvas.animating = false
        if(isMyTurn.value){
            
        }
    }
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
    rows.push(createScoreCardRow("Three of kind [18]", 14, HandType.THREE_OF_KIND, initialRowHeight))
    rows.push(createScoreCardRow("Full house [28]", 15, HandType.FULL_HOUSE, initialRowHeight))
    rows.push(createScoreCardRow("Low straight (1-5) [15]", 16, HandType.SMALL_STRAIGHT, initialRowHeight))
    rows.push(createScoreCardRow("High straight (2-6) [20]", 17, HandType.LARGE_STRAIGHT, initialRowHeight))
    rows.push(createScoreCardRow("Four of kind [24]", 18, HandType.FOUR_OF_KIND, initialRowHeight))
    rows.push(createScoreCardRow("Chance [30]", 19, HandType.CHANCE, initialRowHeight))
    rows.push(createScoreCardRow("Yatzy [50]", 20, HandType.YATZY, initialRowHeight))
    rows.push(createScoreCardRow("Grand Total [374]", 21, HandType.TOTAL, initialRowHeight))
    return rows
}
const initTable = (): IYatzyTable => {
    let initialTable: IYatzyTable = store.getters.yatzyTable
    console.log("initTable:" + JSON.stringify(initialTable))
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
    if (!props.watch) { // no scoreCard reseting for watchers 
        initialTable.players.forEach(player => {
            player.scoreCard = { bonus: undefined, subTotal: 0, total: 0, hands: [], lastAdded: null }
            player.enabled = true
        })
    }
    initialTable.players = players
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
const isChatVisible = computed<boolean>(() =>{ return verticalLayout.value === true && screen.width >= 1200 })
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
const verticalLayout =ref<boolean>(true)
const resignButtonClicked = ref<boolean>(false)

const resignButtonClick = () =>{
    resign(yatzyTable.value.tableId)
    resignButtonClicked.value = true
}
const isResignButtonEnabled = computed<boolean> (() =>
    yatzyTable.value.gameOver === false && 
    yatzyTable.value.players.find(player => player.scoreCard.hands.length > 0 && player.name === userName.value && player.enabled ===true) != null
    && resignButtonClicked.value === false
)
const isRematchButtonEnabled = computed<boolean> (() =>
    yatzyTable.value.gameOver === true && yatzyTable.value.players.length > 1
)
watch(() => queueLength, (newVal) => {
    if (newVal.value > 0) {
        consumeActions()
    }
}, { deep: true })

const imPlaying = () =>{
    return yatzyTable.value.players.find(player => player.name === userName.value) != null
}
const consumeActions = () => {
    if (actionQueue.value.blocked || actionQueue.value.actions.length === 0) {
        console.log("actionQueue not consuming " + actionQueue.value.actions.length)
        return
    }
    if(yatzyTable.value.playerInTurn === null){
        console.log("consumeActions no playerInTurn, so game over")
        return
    }
    actionQueue.value.blocked = true
    const action: IYatzyAction = actionQueue.value.actions.splice(0, 1)[0]
    expectingServerResponse = false
    if (action.type === "yatzyRollDices") {
        animateDices(yatzyTable.value.dices, action.payload.yatzy.dices).then(() => {
            const player = <IYatzyPlayer>yatzyTable.value.playerInTurn
            playerInTurn.value.rollsLeft = action.payload.table.playerInTurn.rollsLeft
            setDiceNumbers(yatzyTable.value.dices, action.payload.table.dices, player.rollsLeft === 0)
            drawAll()
            unblockQueue()
            console.log("animations done")
        })
    } else if (action.type === "yatzySelectHand") {
        updateScoreCard(action.payload)
        repaintScoreCard()
        if (action.payload.table.gameOver) {
            handleGameOver()
        } else {
            unblockQueue()
        }
    } else if (action.type === "changeTurn") {
        store.dispatch("changeTurn", action.payload.table.playerInTurn).then(() => {
            yatzyTable.value.dices.forEach(dice => {
                dice.selected = false
            })
            unblockQueue()
            document.body.style.cursor = "default"
            yatzyTable.value.playerInTurn = <IYatzyPlayer> action.payload.table.playerInTurn
            playerInTurn.value.rollsLeft = action.payload.table.playerInTurn.rollsLeft
            if (isMyTurn.value) {
                yatzyTable.value.canvas.enabled = true
            }           
            repaintYatzyTable()
        })
    } else if (action.type === "timeout") {
        let yatzyPlayer = <IYatzyPlayer>yatzyTable.value.players.find(player => player.name === action.payload.table.timedOutPlayerName)
        console.log("Handling timeout"+JSON.stringify(yatzyPlayer) +" myName:"+userName +" timedOut "+action.payload.table.timedOutPlayerName)
        deactivatePlayer(yatzyPlayer, action, true)
    }
}

const deactivatePlayer = (yatzyPlayer:IYatzyPlayer, action:any, timedOut:boolean) =>{
    yatzyPlayer.enabled = false
    if (userName.value === yatzyPlayer.name && timedOut === true) {
           iTimedOut = true
    }
    if(userName.value === yatzyPlayer.name) {
        yatzyTable.value.canvas.enabled = false
    }
    repaintScoreCard()
    if (action.payload.table.gameOver) {
          handleGameOver()
    } else {
        unblockQueue()
    }
}
const handleGameOver = () => {
    stopPlayerTimeInterval()
    console.log("Handling gameOver")
    yatzyTable.value.playerInTurn = null
    actionQueue.value.blocked = true
    actionQueue.value.actions = []
    yatzyTable.value.canvas.enabled = false
    yatzyTable.value.gameOver = true
    const message: IChatMessage = { from: "System", text: "Game ended" }
    store.dispatch("chat", message)
    drawAll()
}
let playerTimeInterval = null
//let secondsLeft = null
const stopPlayerTimeInterval = () => {
    playerTimeInterval = clearInterval(playerTimeInterval)
}
const startCountdownTimer = (seconds: number) => {
    console.log("restartInterval " + seconds)
    if (playerTimeInterval) {
        stopPlayerTimeInterval()
    }
    yatzyTable.value.secondsLeft = seconds
    playerTimeInterval = setInterval(() => {
        yatzyTable.value.secondsLeft--
        if (yatzyTable.value.secondsLeft <= 0 || !yatzyTable.value.secondsLeft)
            stopPlayerTimeInterval()
    }, 1000)
}
const updateScoreCard = (payload: any) => {
    let yatzyPlayer: IYatzyPlayer = yatzyTable.value.players.find(player => player.name === payload.yatzy.whoPlayed)
    yatzyPlayer.scoreCard.total = payload.yatzy.scoreCard.total
    yatzyPlayer.scoreCard.bonus = payload.yatzy.scoreCard.bonus
    yatzyPlayer.scoreCard.subTotal = payload.yatzy.scoreCard.subTotal
    yatzyPlayer.scoreCard.lastAdded = payload.yatzy.scoreCard.lastAdded
    let lastHand = yatzyPlayer.scoreCard.hands.find(hand => hand.last === true)
    if (lastHand && lastHand.last) {
        lastHand.last = false
    }
    yatzyPlayer.scoreCard.hands.push({ handType: payload.yatzy.scoreCard.lastAdded.handType, typeNumber: payload.yatzy.scoreCard.lastAdded.typeNumber, value: payload.yatzy.scoreCard.lastAdded.value, last: true })
}
const unblockQueue = (timeout: number = 0) => {
    expectingServerResponse = false
    actionQueue.value.blocked = false
    setTimeout(() => {
        consumeActions()
    }, timeout)
}

const setCanvasPositionToLeft = () => {
    yatzyTable.value.canvas.element.style.position = 'absolute'
    yatzyTable.value.canvas.element.style.left = '0px'
}

const setDefaultCanvasPosition = () => {
    yatzyTable.value.canvas.element.style.position = 'relative '
    yatzyTable.value.canvas.element.style.left = ''
}

const resizeDocument = () => {
    //Resizes canvas size and sets position
    console.log("resizeEvent screenWidth" + screen.width)
    setTableLayout()
    setupCanvas()
    drawAll()
}



const setTableLayout = () => {
    verticalLayout.value = true
    if(yatzyTable.value.players.length ===4 ){
        verticalLayout.value = false
    }
    if (window.innerWidth >= 1200) {
        yatzyTable.value.canvas.element.width = 1100
        yatzyTable.value.canvas.element.height = window.innerHeight >= 1100 ? 1100: window.innerHeight
        setDefaultCanvasPosition()
    } else if (window.innerWidth  >= 1000 && yatzyTable.value.players.length > 2) {
        yatzyTable.value.canvas.element.width = 900
        yatzyTable.value.canvas.element.height = window.innerHeight >= 1100 ? 1100: window.innerHeight
        setDefaultCanvasPosition()
    }
    else if (window.innerWidth >= 940) {
        if (yatzyTable.value.playerAmount >= 3) {
             verticalLayout.value = false
        }
        setCanvasPositionToLeft()
        yatzyTable.value.canvas.element.width = window.innerWidth
        yatzyTable.value.canvas.element.height = window.innerHeight >= 1100 ? 1100: window.innerHeight
    }
    else {
        verticalLayout.value = false
        yatzyTable.value.canvas.element.width = window.innerWidth -10
        yatzyTable.value.canvas.element.height = window.innerHeight -10
        setCanvasPositionToLeft()
    }
    yatzyTable.value.scoreCardRows = initScoreCardRows()
    const size = diceSize()
    yatzyTable.value.dices.forEach((dice) => {
        dice.image.canvasDimension.x = size.x
        dice.image.canvasDimension.y = size.y
    })
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
    const obj = { title: "YATZY_SELECT_HAND", message: yatzyTable.value.tableId, yatzy: { handType: highlightedRow.handType } }
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

const markLastHands = () => {
     yatzyTable.value.players.forEach(tablePlayer => {
            let snapShotPlayer:IYatzyPlayer = gameSnapshot.table.players.find(snapShotPlayer => snapShotPlayer.name === tablePlayer.name)
            if(snapShotPlayer.enabled === false){
                tablePlayer.enabled = false
            }
    })
}
const createTableFromSnapShot = () => {
    if (gameSnapshot === null) {
        unblockQueue()
        //No turns yet, coming back to board when nothing is played yet
        return
    } 
    console.log("FROM SNAPSHOT")
    actionQueue.value.actions = []
    setDiceNumbers(yatzyTable.value.dices, gameSnapshot.table.dices, false)
    gameSnapshot.table.players.forEach(snapshotPlayer => {
        const tablePlayer: IYatzyPlayer = yatzyTable.value.players.find(player => player.name === snapshotPlayer.name)
        const snapshotPlayerHands: IHand[] = snapshotPlayer.scoreCard.hands
        tablePlayer.scoreCard.hands = []
        for (const [key, value] of Object.entries(snapshotPlayerHands)) {
            let hand:IHand = value
            if(snapshotPlayer.scoreCard?.lastAdded?.handType === hand.handType){
                hand.last = true
            }
            tablePlayer.scoreCard.hands.push(value)
        }
        tablePlayer.scoreCard.bonus = snapshotPlayer.scoreCard.bonus
        tablePlayer.scoreCard.total = snapshotPlayer.scoreCard.total
        tablePlayer.scoreCard.subTotal = snapshotPlayer.scoreCard.subTotal
        tablePlayer.enabled = snapshotPlayer.enabled
    })
    
    let player: IYatzyPlayer = <IYatzyPlayer>gameSnapshot.table.playerInTurn
    if (playerInTurn.value) {
        playerInTurn.value.rollsLeft = player?.rollsLeft
        
        console.log("ROLLS LEFT:"+playerInTurn.value.rollsLeft +" PlayerInTurn:"+JSON.stringify(playerInTurn.value))
    }
    let me: IYatzyPlayer = gameSnapshot.table.players.find(player => !player?.enabled && player?.name === userName.value)
    if (me) {
        iTimedOut = true
    }
    if (gameSnapshot.table.gameOver) {
        return handleGameOver()
    }
    const action = { payload: gameSnapshot }
    initNewTurnIfRequired(action)

}
const onVisibilityChange = () => {
    console.log("onVisiblityChange " + isDocumentVisible())
    if (isDocumentVisible()) {
        createTableFromSnapShot()
        drawAll()
        unblockQueue()
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
    if (!row || !isHighlightableRow(row)) {
        document.body.style.cursor = "default"
        return
    }
    if (playerInTurn.value.scoreCard.hands.length === undefined) {
        highlightedRow = row
        return
    }
    let index = playerInTurn.value.scoreCard.hands.findIndex(hand => {
        hand.handType === row.handType
    })
    index === -1 ? highlightedRow = row : ""
}
const isHighlightableRow = (row: IScoreCardRow) => {
    if (row.handType === HandType.BONUS || row.handType === HandType.SUBTOTAL || row.handType === HandType.TOTAL) {
        return false
    }
    const scoreCard: IScoreCard = yatzyTable.value.players.find(player => player.name === userName.value).scoreCard
    const hand: IHand = scoreCard.hands.find(hand => hand.handType.toString() === HandType[row.handType].toString())
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
onUnmounted(() => {
    console.log("onUnmounted")
    unsubscribeAction()
    unsubscribe()
    document.removeEventListener("visibilitychange", onVisibilityChange)
    leaveTable(yatzyTable.value.tableId)
})

const unsubscribe = store.subscribe((mutation, state) => {
    if (mutation.type === "changeTurn") {
        console.log("changePlayer mutation")
    }
    if (mutation.type === "rematch") {
        console.log("rematch sub")

    }
    if (mutation.type === "resign") {
        console.log("resign sub")
    }
})

const startRematch = (action) => {
    startCountdownTimer(action.payload.table.secondsLeft)
    yatzyTable.value = initTable()
    let playerInTurn:IYatzyPlayer = <IYatzyPlayer> action.payload.table.playerInTurn
    playerInTurn.rollsLeft = action.payload.table.playerInTurn.rollsLeft
    yatzyTable.value.players.forEach(player => player.enabled = true)
    yatzyTable.value.playerInTurn = playerInTurn
    iTimedOut = false
    yatzyTable.value.gameOver = false
    unblockQueue()
    setupCanvas()
    resignButtonClicked.value = false
    drawAll()
}
const unsubscribeAction = store.subscribeAction((action, state) => {
    console.log("Action in:" + JSON.stringify(action))
    if (!isYatzyTableRelatedAction(action)) {
        return
    }
    if(action.type === "currentTableIsClosed"){
        yatzyTable.value.players.forEach(player =>{
            player.enabled = false
        })
        handleGameOver()
        return // Only watchers can exist in table
    }
    if(action.type === "rematch"){
        startRematch(action)
        gameSnapshot = action.payload
        return
    }
    if (action.type === "leaveTable") {
        gameSnapshot = action.payload
        return handleLeavingPlayer(action)
    }
    if(action.type === "multiplayerTableResign"){
        gameSnapshot = action.payload
        let yatzyPlayer = <IYatzyPlayer>yatzyTable.value.players.find(player => player.name === action.payload.who.name)
        deactivatePlayer(yatzyPlayer, action, false) 
        const newTurnInited = initNewTurnIfRequired(action)
        if(newTurnInited){
            startCountdownTimer(action.payload.table.secondsLeft)
        }
        return
    }
    gameSnapshot = action.payload
    startCountdownTimer(action.payload.table.secondsLeft)
    actionQueue.value.actions.splice(actionQueue.value.actions.length, 0, action)
    initNewTurnIfRequired(action)
})
const isYatzyTableRelatedAction = (action: any) => {
    return action.type === "yatzyRollDices" || action.type === "yatzySelectHand" || action.type === "leaveTable" || action.type === "timeout"
    || action.type === "rematch" || action.type === "multiplayerTableResign" || action.type ==="currentTableIsClosed"
}
const handleLeavingPlayer = (action: any) => {
    let player: IYatzyPlayer = yatzyTable.value.players.find(player => player.name === action.payload.who.name)
    if (!player) {
        // watcher left
        console.log("watcher left")
        return
    }
    console.log("handle leaving player")
    player.enabled = false
    unblockQueue()
    initNewTurnIfRequired(action)
    drawAll()
}

const initNewTurnIfRequired = (action):boolean => {
    if (action.payload.table.gameOver) {
        return false
    }
    //One person can play to the end if others timeouts or leaves
    if (action.payload?.table?.playerInTurn?.name !== yatzyTable.value?.playerInTurn?.name || yatzyTable.value?.players.filter(player => player.enabled === true).length === 1) {
        let changeTurnAction = { type: "changeTurn", payload: action.payload }
        actionQueue.value.actions.splice(actionQueue.value.actions.length, 0, changeTurnAction)
        return true
    }
    return false
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

const getDiceHorizontalGapFromDiceSectionStart = () =>{
    const width = yatzyTable.value.canvas.element.width
    if(width < 480){
        return 1
    }
    if(width <500) {
        return 10
    }
    return yatzyTable.value.canvas.element.width <550? 20:60
}

const dicesPixelsDown = ():number => {
    const size: IVector2 = diceSize()
    return yatzyTable.value.canvas.element.width <550 ?size.y /2: size.y
}
const repaintDices = () => {
    const section = diceSection()
   // yatzyTable.value.canvas.ctx.rect(section.start.x,section.start.y, section.end.x,section.end.y)
   // yatzyTable.value.canvas.ctx.stroke()
    const size: IVector2 = diceSize()
    const diceGapFromDiceSectionStart = getDiceHorizontalGapFromDiceSectionStart()

    const pixelsDown = dicesPixelsDown()
 
    for (let i = 0; i < 5; i++) {
        const dice = yatzyTable.value.dices[i]
        const size: IVector2 = diceSize()
        if (dice.selected) {
            //position down size of one dice
            dice.position = { x: section.start.x + i * size.x + diceGapFromDiceSectionStart, y: section.start.y + pixelsDown }
        } else if (playerInTurn?.value?.rollsLeft === 0 && dice.selected === false) {
            dice.position = { x: section.start.x + i * size.x + diceGapFromDiceSectionStart, y: section.start.y + (dice.position.y - section.start.y) }
        } else {
            //basic position
            dice.position = { x: section.start.x + i * size.x + diceGapFromDiceSectionStart, y: section.start.y }
        }
        dice.image.canvasDestination = dice.position
        dice.image.canvasDimension.x = size.x
        dice.image.canvasDimension.x = size.y
        repaintComponent(dice)
    }
}

const repaintButtons = () => {
    //yatzyTable.value.canvas.ctx.rect(buttonSection().start.x,buttonSection().start.y, buttonSection().end.x,buttonSection().end.y)
     //yatzyTable.value.canvas.ctx.stroke()
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
let itYourTurn = "It's your turn " + userName.value
const waitingTurnText = "Waiting your turn"
const youHaveTimedOut = "You timed out"

let iTimedOut = false

const optionsText = (): string => {
    if (iTimedOut) {
        return youHaveTimedOut
    }
    if (isMyTurn.value === false && iTimedOut === false && !props.watch) {
        return waitingTurnText
    }
    if (isMyTurn.value === false) {
        return ""
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
    const x = diceSection().start.x + getDiceHorizontalGapFromDiceSectionStart()
    const turnInfoY = diceSection().start.y - 30
    const y = diceSection().start.y - 10
    const ctx = yatzyTable.value.canvas.ctx
    ctx.beginPath()
    ctx.font = "18px bolder Arial"
    if (!playerInTurn.value) {
        ctx.fillText("GAME OVER", x, turnInfoY)
        ctx.closePath()
        return
    }
    isMyTurn.value === true ? ctx.fillStyle = "green" : ctx.fillStyle = "red"
    ctx.fillText(isMyTurn.value === false ? "In turn " + playerInTurn?.value?.name + " (left = " + playerInTurn?.value?.rollsLeft + ")" : itYourTurn + " (left = " + playerInTurn?.value?.rollsLeft + ")", x, turnInfoY)
    ctx.fillStyle = "black"
    ctx.fillText(optionsText(), x, y)
    ctx.closePath()
}

const scoreCardSection = (): ISection => {

    const canvasWidth = yatzyTable.value.canvas.element.width
    const canvasHeight = yatzyTable.value.canvas.element.height
    const start: IVector2 = { x: 0, y: 0 }
    let end: IVector2 = undefined
    if(yatzyTable.value.players.length === 4 && canvasWidth >=1100){
        let x = canvasWidth * 0.90
        let y = canvasHeight * 0.5
        end = { x: x, y: y }
        return { start, end }
    }
    if(yatzyTable.value.players.length === 4){
        let x = canvasWidth 
        let y = canvasHeight * 0.65
        end = { x: x, y: y }
        return { start, end }
    }
    if (canvasWidth >= 1200 || canvasWidth >= 1000 && yatzyTable.value.players.length <= 3) {
        let x = canvasWidth * 0.95 / 2
        let y = x
        end = { x: x, y: y }
        return { start, end }
    }
    if (canvasWidth >= 768 && yatzyTable.value.players.length <= 3) {
        let x = canvasWidth * 0.55
        let y = x
        end = { x: x, y: y }
        return { start, end }
    }
    const x = canvasWidth
    const y = yatzyTable.value.canvas.element.height * 0.75
    end = { x: x, y: canvasHeight * 0.75 }
    // yatzyTable.value.image.canvasDimension = {x:x, y:y}
    return { start, end }

}
const diceSection = (): ISection => {
    const dSize: IVector2 = diceSize()
    const canvasWidth = yatzyTable.value.canvas.element.width
    scoreCardSection().end
 
    if(verticalLayout.value){
        const x = scoreCardSection().end.x
        const y = yatzyTable.value.canvas.element.height *0.12
        return { start: { x: x, y: y }, end: { x: x + 6 * dSize.x, y: y + (dSize.y * 2) } }
    }
    const spaceBetweenScoreCardAndDiceSection = 50 //canvasWidth>=600 ?75:5
    const x = 20
    const y= scoreCardSection().end.y +spaceBetweenScoreCardAndDiceSection
   
    return { start: { x: x, y: y }, end: { x: 20 + 6 * dSize.x, y: y+ dSize.y *2} }
}

const buttonSection = (): ISection => {
    const dices: ISection = diceSection()
    const dSize = diceSize()
    const canvasWidth = yatzyTable.value.canvas.element.width
    const canvasHeight = yatzyTable.value.canvas.element.height
    if(!verticalLayout.value && canvasWidth < 440){ // TODO
        const buttonSectionStart: IVector2 = { x: dices.end.x -100, y: dices.start.y-30 }
        const buttonSectionEnd: IVector2 = {x: dices.end.x, y: dices.start.y}
        return { start: buttonSectionStart, end: buttonSectionEnd }
    } 
    if(!verticalLayout.value && canvasWidth < 530){
        const buttonSectionStart: IVector2 = { x: dices.end.x -80, y: dices.start.y-30 }
        const buttonSectionEnd: IVector2 = {x: dices.end.x +20, y: dices.start.y}
        return { start: buttonSectionStart, end: buttonSectionEnd }
    } 
    if(!verticalLayout.value){
        const buttonSectionStart: IVector2 = { x: dices.end.x , y: dices.start.y }
        const buttonSectionEnd: IVector2 = { x: canvasWidth, y: dices.start.y+75 }
        return { start: buttonSectionStart, end: buttonSectionEnd }
    } 
    const buttonSectionStart: IVector2 = { x: dices.start.x +50, y: dices.end.y+20 }
    const buttonSectionEnd: IVector2 = { x: dices.start.x +150, y: dices.end.y +75 }

    return { start: buttonSectionStart, end: buttonSectionEnd }  
}

const diceSize = (): IVector2 => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    if (verticalLayout.value && screen.width >= 1000) {
        return { x: 85, y: 85 }
    }
    if (verticalLayout.value && window.innerWidth < 1000) {
        return { x: 70, y: 70 }
    }
    if(canvasWidth < 600){
        return { x: 65, y: 65 }
    }
    return window.innerWidth < 550 ? { x: 54, y: 54 } : { x: 80, y: 80 }
}
let animationRun = 0
const delay = millis => new Promise(resolve => setTimeout(resolve, millis))

const drawAll = () => {
   repaintYatzyTable()
}
const animateDices = async (tableDices: IDice[], resultDices: IDice[]) => {
    if(gameOptions.value.animations ===false){
        return
    }
    yatzyTable.value.canvas.animating = true
    if (animationRun !== 0) {
        randomizeDices(yatzyTable.value.dices)
        repaintDices()
    } else {
        prepareDicesForRoll(tableDices, resultDices)
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
const prepareDicesForRoll = async (tableDices: IDice[], resultDices: IDice[]) => {
    tableDices.forEach(tableDice => {
        let dice: IDice = resultDices.find(resultDice => tableDice.diceId === resultDice.diceId)
        if (dice) {
            tableDice.selected = false
        } else {
            tableDice.selected = true
        }
    })
    await delay(1000)
}

const setDiceNumbers = async (tableDices: IDice[], resultDices: IDice[], animate: boolean = false) => {
    resultDices.forEach(resultDice => {
        const tableDice: IDice = tableDices.find(tableDice => tableDice.diceId === resultDice.diceId)
        setDiceNumber(tableDice, resultDice.number)
    })
    if (animate === false || !gameOptions.value.animations) {
        return
    }
    let counter = 0
    const finalDices: IDice[] = tableDices.filter(tableDice => !tableDice.selected)
    const size: IVector2 = diceSize()
    yatzyTable.value.canvas.animating = true
    const pixelsDown = dicesPixelsDown()
    while (counter < 16) {
        for (let i = 0; i < finalDices.length; i++) {
            let finalDice = finalDices[i]
            finalDice.position.y += pixelsDown / 16
        }
        drawAll()
        await delay(50)
        counter++
    }
    yatzyTable.value.canvas.animating = false
}

const setDiceNumber = (dice: IDice, number: number) => {
    dice.number = number
    dice.image.image = <HTMLImageElement>document.getElementById("dice" + number)

}

const setRandomNumber = (dice: IDice) => {
    if (!dice.selected) {
        setDiceNumber(dice, getRandomNumber())
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

const nameColumnLength: number = 120
const fillPlayerScore = (player: IYatzyPlayer, hand: IHand, startPoint: IVector2) => {
    const ctx = yatzyTable.value.canvas.ctx
    if (hand && hand.value > -1) {
        let handValue = hand.value > -1 ? hand.value.toString() : " - "
        ctx.fillStyle = player.enabled ? "black" : "gray"
        ctx.fillText(handValue, startPoint.x, startPoint.y)
    }
    ctx.fillStyle = "black"
}
const scorecardRowStart = 25
const scoreCardHandTextsEnd = 200
const gapBetweenScoreCardRowTitleAndLine = 15

const drawScoreCardRow = (row: IScoreCardRow, rowHeight: number) => {
    const ctx = yatzyTable.value.canvas.ctx

    ctx.fillText(row.title, row.section.start.x, rowHeight * row.nth + gapBetweenScoreCardRowTitleAndLine)
    fillPlayersPointsOnRow(row, rowHeight)
   // ctx.moveTo(row.section.start.x, row.section.start.y)
    //ctx.lineTo(row.section.end.x, row.section.end.y)
    ctx.moveTo(row.section.start.x,  rowHeight * row.nth) 
    ctx.lineTo(row.section.end.x,  rowHeight * row.nth)
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
        const startPoint: IVector2 = { x: scoreCardHandTextsEnd + 50 + nameColumnLength * i, y: rowHeight * (row.nth + 1) - 5 }
        fillPlayerScore(yatzyTable.value.players[i], hand, startPoint)
        if (hand && hand.last) {
            const ctx = yatzyTable.value.canvas.ctx
            ctx.fillStyle = "red"
            ctx.font = '900 24px Arial'
            ctx.fillText(" *", startPoint.x + 20, startPoint.y + 5)
            ctx.font = "bold " + FONT_SIZE.DEFAULT + " Arial"
            ctx.fillStyle = "black"
        }
    }
}

const createScoreCardRow = (title: string, nthRow: number, handType: HandType, height: number): IScoreCardRow => {
    let sc: ISection = scoreCardSection()
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
    if (canvas.element.width < 768 ) {
        ctx.font = FONT_SIZE.DEFAULT + " Arial"
    }
    yatzyTable.value.scoreCardRows.forEach(row => {
        drawScoreCardRow(row, rowHeight)
    })
    //Vertical lines
    for (let i = 0; i < yatzyTable.value.players.length; i++) {
        ctx.fillStyle = yatzyTable.value.players[i].enabled ? "black" : "gray"
        let playerName:string = yatzyTable.value.players[i].name
        if(playerName.length > 10){
            let playerNamePart1:string = playerName.substring(0, 10)
            let playerNamePart2:string = playerName.substring(10)
            ctx.fillText(playerNamePart1, scoreCardHandTextsEnd + 8 + i * nameColumnLength, rowHeight * 2.5)
            ctx.fillText(playerNamePart2, scoreCardHandTextsEnd + 8 + i * nameColumnLength, rowHeight * 3)
        }
        else {
            ctx.fillText(playerName, scoreCardHandTextsEnd + 5 + i * nameColumnLength, rowHeight * 3)
        }

        ctx.moveTo(scoreCardHandTextsEnd + i * nameColumnLength, rowHeight * 3)
        ctx.lineTo(scoreCardHandTextsEnd + i * nameColumnLength, rowHeight * 21)
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
.yatzyLeft {
    position: absolute;
    left: 0px
}
</style>
