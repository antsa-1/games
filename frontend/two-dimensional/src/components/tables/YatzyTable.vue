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
            <span v-if="yatzyTable" class="text-success">Playing turn </span>
            <span v-else-if="yatzyTable.playerInTurn?.name === userName" class="text-success"> It's your turn
                {{ userName }} > time left:{{ yatzyTable.secondsLeft }} </span>
            <span v-else-if="yatzyTable?.playerInTurn === null" class="text-success"> Game ended </span>
            <div v-else class="text-danger"> In turn: {{ yatzyTable.playerInTurn?.name }}</div>
        </div>
    </div>


    <div>
        <canvas id="canvas" width="1200" height="600" style="border:1px solid"></canvas>
    </div>


</template>

<script setup lang="ts">

import { IPlayer, IUser } from '@/interfaces/interfaces';
import { IYatzyComponent, IYatzyTable } from '@/interfaces/yatzy';
import { ref, computed, onMounted, onBeforeMount, onUnmounted, watch, ComputedRef, } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { IGameOptions, Image, IVector2, IGameCanvas, FONT_SIZE, FONT, IAction } from "../../interfaces/commontypes"
import { IYatzyPlayer, IYatzyMessage, IDice, ISection, IYatzySnapshot, IYatzyAction, IYatzyActionQueue, HandType,IHand } from "../../interfaces/yatzy"
import Chat from "../Chat.vue"
const CANVAS_ROWS = 22
const router = useRouter()
let tableSnapshot: IYatzySnapshot = undefined
const store = useStore()
const gameOptions = ref<IGameOptions>({ notificationSound: false })


onMounted(() => {
    let canvasElement = <HTMLCanvasElement>document.getElementById("canvas")
    yatzyTable.value.canvas = <IGameCanvas>{ element: canvasElement, animating: false, ctx: canvasElement.getContext("2d") }
    yatzyTable.value.canvas.ctx.imageSmoothingEnabled = true
    if (isMyTurn) {
        yatzyTable.value.canvas.enabled = true
    }
    attachListeners()
    repaintYatzyTable()
})


const initTable = (): IYatzyTable => {
    console.log("initTable:")
    const bgSize: IVector2 = { x: 1200, y: 1200 }
    const tableImage = <Image>{
        image: <HTMLImageElement>document.getElementById("yatzybg"),
        canvasDimension: bgSize,
        canvasDestination: <IVector2>{ x: 0, y: 0 },
        realDimension: { x: 1024, y: 1024 },
        canvasRotationAngle: 0,
        visible: true
    }
    let initialTable: IYatzyTable = store.getters.yatzyTable
    const players: IYatzyPlayer[] = initialTable.players
    initialTable.players.forEach(player => {
        player.scoreCard = {bonus:undefined, subTotal:undefined, total:undefined, hands:[]}
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
    console.log("dices:" + initialTable.dices)
    initialTable.image = tableImage
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
const isMyTurn = computed<boolean>(() => playerInTurn.value.name === userName.value)
const isRollButtonVisible = computed<boolean>(() => isMyTurn.value && playerInTurn.value.rollsLeft > 0 && yatzyTable.value.canvas?.animating === false)
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
    //console.log("Action queue consuming" + actionQueue.value + " " + JSON.stringify(actionQueue.value))
    const action: IYatzyAction = actionQueue.value.actions.splice(0, 1)[0]
    if (action.actionName === "yatzyRollDices") {
        console.log("ActionIn:" + JSON.stringify(action))
        animateDices(yatzyTable.value.dices, action.rollResult).then(() => {
            const player = <IYatzyPlayer>yatzyTable.value.playerInTurn
            player.rollsLeft = action.rollsLeftAfterAction
            setupDices(yatzyTable.value.dices, action.rollResult, player.rollsLeft === 0)
            drawAll()
            unblockQueue()
        })
        console.log("animations done")

    } else if (action.actionName === "options") {
        console.log("Actions to choose from")
    }
}

const unblockQueue = (timeout: number = 0) => {
    actionQueue.value.blocked = false
    setTimeout(() => {
        consumeActions()
    }, timeout)
}

onUnmounted(() => {
    console.log("onUnmounted")
    //unsubscribe()
    //unsubscribeAction()
    //learIntervals()
    //removeMouseListeners()
    //window.removeEventListener("resize", this.resize)
    //document.removeEventListener("visibilitychange", this.onVisibilityChange)
    const obj = { title: "LEAVE_TABLE", message: yatzyTable.value.tableId }
    store.getters.user.webSocket?.send(JSON.stringify(obj))
    store.dispatch("clearTable")
})



const resizeDocument = () => {
    console.log("resizeEvent" + screen.width)
    if (screen.width >= 1200) {
        yatzyTable.value.canvas.element.width = 1100
        yatzyTable.value.canvas.element.height = screen.height
        yatzyTable.value.image.canvasDimension.x = 1100
        yatzyTable.value.image.canvasDimension.y = screen.height
    }
    else if (screen.width >= 768) {
        yatzyTable.value.canvas.element.width = screen.width * 0.9
        yatzyTable.value.canvas.element.height = screen.height
        yatzyTable.value.image.canvasDimension.x = screen.width * 0.9
        yatzyTable.value.image.canvasDimension.y = screen.height
    }
    else if (screen.width >= 600) {
        yatzyTable.value.canvas.element.width = 600
        yatzyTable.value.canvas.element.height = 800
        yatzyTable.value.image.canvasDimension.x = 600
        yatzyTable.value.image.canvasDimension.y = 800
    }
    const size = diceSize()
    yatzyTable.value.dices.forEach((dice) => {
        dice.image.canvasDimension.x = size.x
        dice.image.canvasDimension.y = size.y
    })
    repaintYatzyTable()
}


const sendRollRequest = () => {
    if (isMyTurn.value !== true || playerInTurn.value.rollsLeft <= 0 || yatzyTable.value.canvas.animating === true) {
        return
    }
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

const sendHandSelection = () => {
    if (isMyTurn.value !== true || playerInTurn.value.rollsLeft === 3 || yatzyTable.value.canvas.animating === true) {
        return
    }

    const obj = { title: "YATZY_SELECT_HAND", message: yatzyTable.value.tableId, yatzy: { hand: 0 } }
    console.log("*** Sending selection " + JSON.stringify(obj))
    user.value.webSocket.send(JSON.stringify(obj))
}

const attachListeners = () => {
    window.addEventListener('resize', resizeDocument)

    const canvas = document.getElementById("canvas")
    canvas.addEventListener('pointerover', (event) => {
        console.log('Pointer moved in')
        if (!isMouseEnabled.value) {
            return
        }
        const cursorPoint: IVector2 = { x: event.offsetX, y: event.offsetY }
        if (isPointOnSection(cursorPoint, scoreCardSection())) {
            console.log("scorecard section")
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
            sendRollRequest()
        }
        document.body.style.cursor = "default"
        repaintYatzyTable()
    })
    canvas.addEventListener('pointermove', (event) => {
        if (!isMouseEnabled.value) {
            return
        }
        const cursorPoint = { x: event.offsetX, y: event.offsetY }
        document.body.style.cursor = "default"
        if (isAllowedToSelectDice.value === true && isPointOnSection(cursorPoint, diceSection())) {
            const dice: IDice = getDice(cursorPoint)
            if (dice) {
                document.body.style.cursor = "pointer"
                repaintYatzyTable()
            }
        } else if (isPointOnSection(cursorPoint, buttonSection())) {
            if (isRollButtonVisible.value === true && isPointOnImage(cursorPoint, yatzyTable.value.playButton.image)) {
                document.body.style.cursor = "pointer"
                repaintYatzyTable()
            }
        }
    })
    // canvas.addEventListener("pointerdown", this.handleMouseDown, false)
    //	canvas.addEventListener("pointerup", this.handleMouseUp, false)
    //canvas.addEventListener("pointermove", this.handleMouseMove, false)
    canvas.addEventListener("contextmenu", (e) => e.preventDefault())
}

const getDice = (cursorPoint: IVector2) => {
    return yatzyTable.value.dices.filter(dice => filter(dice, cursorPoint))[0]
}


const filter = (dice: IDice, cursorPoint: IVector2) => {
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
/*
const isPointOnScoreCard = (point: IVector2) => {

    if (point.x < image.canvasDestination.x || point.x > image.canvasDestination.x + image.canvasDimension.x) {
        return false
    }
    if (point.y < image.canvasDestination.y || point.y > image.canvasDestination.y + image.canvasDimension.y) {
        return false
    }
    return true
}
*/
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
    if (mutation.type === "rematch") {
        console.log("rematch sub")
    }
    if (mutation.type === "resign") {
        console.log("resign sub")
    }
})

const unsubscribeAction = store.subscribeAction((action, state) => {
    tableSnapshot = action.payload
    const createdAction: IYatzyAction = createAction(action)
    actionQueue.value.actions.splice(actionQueue.value.actions.length, 0, createdAction)
})

const createAction = (action): IYatzyAction => {
    if (action.type === "yatzyRollDices") {
        return {
            actionName: "yatzyRollDices", rollsLeftAfterAction: action.payload.table.playerInTurn.rollsLeft,
            rollResult: action.payload.yatzy.dices, whoPlayed: action.payload.yatzy.whoPlayed, nextTurnPlayer: action.payload.table.playerInTurn.name
        }
    } else if (action.type === "selectHand") {
        console.log("hand selected ")
    }
    return
}
const repaintComponent = (component: IYatzyComponent) => {

    if (!component) {
        return
    }
    const ctx = yatzyTable.value.canvas.ctx
    if (!component.image.visible) {
        return
    }
    //	ctx.translate(component.position.x, component.position.y)
    if (component.image.canvasRotationAngle) {
        ctx.rotate(component.image.canvasRotationAngle)
    }
    //void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
    if (!component.image.canvasDestination) {
        component.image.canvasDestination = <IVector2>{ x: 0, y: 0 }
    }

    ctx.drawImage(component.image.image, 0, 0, component.image.realDimension.x, component.image.realDimension.y, component.image.canvasDestination.x, component.image.canvasDestination.y, component.image.canvasDimension.x, component.image.canvasDimension.y)
    //	ctx.resetTransform()
}

const repaintPlayerInTurn = () => {
    let table: IYatzyPlayer = <IYatzyPlayer>yatzyTable.value.playerInTurn

}

const repaintDices = () => {
    const section = diceSection()
    for (let i = 0; i < 5; i++) {
        const dice = yatzyTable.value.dices[i]
        const size: IVector2 = diceSize()
        if (dice.selected) {
            //position down size of one dice
            dice.position = { x: section.start.x + i * size.x + 60, y: section.start.y + size.y }
        } else if (playerInTurn.value.rollsLeft === 0 && dice.selected === false) {
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
const selectHand = "Select a hand"
const selectOrRoll = "Select a hand, lock dices or roll unlocked dices"
const nowInTurn = playerInTurn.value.name + " is now in turn"
const itYourTurn = "It's your turn " + playerInTurn.value.name
const waitingTurn = "Waiting your turn"

const optionsText = (): string => {
    if (isMyTurn.value === false) {
        return waitingTurn
    }
    if (playerInTurn.value.rollsLeft === 3) {
        return rollDicesText
    }
    if (playerInTurn.value.rollsLeft === 0) {
        return selectHand
    }
    return selectOrRoll
}
const repaintInfoTexts = () => {
    const x = diceSection().start.x + 75
    const turnInfoY = diceSection().start.y - 30
    const y = diceSection().start.y - 10
    const ctx = yatzyTable.value.canvas.ctx
    ctx.beginPath()
    ctx.font = "18px bolder Arial";
    isMyTurn.value === true ? ctx.fillStyle = "green" : ctx.fillStyle = "red"
    ctx.fillText(isMyTurn.value === false ? nowInTurn : itYourTurn, x, turnInfoY)
    ctx.fillStyle = "black"
    ctx.fillText(optionsText(), x, y)
    ctx.closePath()
}

const gameSection = (): ISection => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    const size = diceSize()
    if (canvasWidth >= 1200) {
        const y = yatzyTable.value.canvas.element.height / 10
        return { start: { x: 700, y: y }, end: { x: 700 + 6 * size.x, y: y + (size.y * 2) } }
    }
    if (canvasWidth >= 768) {
        const x = canvasWidth / 2
        const y = 300
        console.log("bbb")
        return { start: { x: x, y: y }, end: { x: x + 6 * size.x, y: y + size.y } }
    }
    const x = 220
    const y = 500
    if (canvasWidth > 600) {
        console.log("ccc")
        return { start: { x: x, y: y }, end: { x: x + 6 * size.x, y: y + size.y } }
    }
    console.log("ddd")
    return { start: { x: x, y: y }, end: { x: x + 6 * size.x, y: y + size.y } }
}

const diceSection = (): ISection => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    const size: IVector2 = diceSize()
    if (canvasWidth >= 1200) {
        const y = yatzyTable.value.canvas.element.height / 10
        return { start: { x: 700, y: y }, end: { x: 700 + 6 * size.x, y: y + (size.y * 2) } }
    }
    if (canvasWidth >= 768) {
        const x = canvasWidth / 2
        const y = 300
        return { start: { x: x, y: y }, end: { x: x + 6 * size.x, y: y + size.y } }
    }
    const x = 220
    const y = 500
    if (canvasWidth > 600) {
        console.log("ccc")
        return { start: { x: x, y: y }, end: { x: x + 6 * size.x, y: y + size.y } }
    }
    console.log("ddd")
    return { start: { x: x, y: y }, end: { x: x + 6 * size.x, y: y + size.y } }
}

const scoreCardHeight = () => {
    const s = scoreCardSection()
    return s.end.y - s.start.y
}
const scoreCardSection = ():ISection => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    const start: IVector2 = { x: 0, y: 0 }
    let end: IVector2 = undefined
    if (canvasWidth >= 1200) {
        const x = yatzyTable.value.canvas.element.width / 2
        end = { x: x, y: yatzyTable.value.canvas.element.height }
        return { start, end }
    }
    if (canvasWidth >= 768 && yatzyTable.value.players.length === 2) {
        const x = yatzyTable.value.canvas.element.width / 2
        end = { x: x, y: yatzyTable.value.canvas.element.height }
        return { start, end }
    }
    const x = yatzyTable.value.canvas.element.width
    end = { x: x, y: yatzyTable.value.canvas.element.height * 0.75 }
    return { start, end }

}

const buttonSection = (): ISection => {
    //button section comes under dice section
    const dices: ISection = diceSection()
    const dSize = diceSize()
    const buttonSectionStart: IVector2 = { x: dices.start.x, y: dices.end.y + dSize.y }
    const buttonSectionEnd: IVector2 = { x: dices.end.x, y: dices.end.y + 2.5 * dSize.y }
    return { start: buttonSectionStart, end: buttonSectionEnd }
}
/*
const buttonSize = ():IVector2 => {
   const canvasWidth = yatzyTable.value.canvas.element.width
    if (canvasWidth > 1000) {
        return { x: 100, y: 50 }
    }
    if (canvasWidth >= 768) {

        return { x: 75, y: 40 }
    }
    return { x: 75, y: 40 } 
}
*/
const diceSize = (): IVector2 => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    if (canvasWidth > 1000) {
        return { x: 80, y: 80 }
    }
    if (canvasWidth >= 768) {

        return { x: 60, y: 60 }
    }
    return { x: 40, y: 40 }
}
let animationRun = 0
const delay = millis => new Promise(resolve => setTimeout(resolve, millis))

const drawAll = () => {
    requestAnimationFrame(repaintYatzyTable)
}
const animateDices = async (tableDices: IDice[], resultDices: IDice[]) => {
    yatzyTable.value.canvas.animating = true
    if (animationRun !== 0) {
        randomizeDices(yatzyTable.value.dices)
        requestAnimationFrame(repaintDices)
    } else {
        randomizeDices(yatzyTable.value.dices)
        console.log("aniamteDices repaintTable")
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
    console.log("diceSelected1:" + playerInTurn.value.rollsLeft)
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

const getPlayersHands = (handType: HandType):IHand[] => {
    let hands:IHand[] = []
    const obj = yatzyTable.value.players.forEach( player =>{
       const hand:IHand = getPlayerHand(handType, player)
       if(hand)
       hands.push(hand)
    })
    return hands
}

const getPlayerHand = (handType: HandType, player:IYatzyPlayer):IHand => {
    return player.scoreCard.hands.find(hand => hand.handType === handType)
}

const getPlayerBonus = (player:IYatzyPlayer):number => {
    return player.scoreCard.bonus
}

const getPlayerSubTotal = (player:IYatzyPlayer):number => {
    return player.scoreCard.subTotal
}

const getPlayerTotal = (player:IYatzyPlayer):number => {
    return player.scoreCard.total
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

const magic:number = 120
const fillPlayerScore = ( hand:IHand, startPoint:IVector2) => {
    const ctx = yatzyTable.value.canvas.ctx
    if(hand){
        const handValue = hand.value? hand.value.toString() : " - "
        ctx.fillText(handValue, startPoint.x, startPoint.y)
    }
}

//quite many lines in this method, split?
const repaintScoreCard = () => {
    const canvas = yatzyTable.value.canvas
    const cardSection = scoreCardSection()
    let rowHeight = scoreCardHeight() / CANVAS_ROWS

    const width = canvas.element.width
    const ctx = canvas.ctx
    ctx.font = "bolder " + FONT_SIZE.LARGEST + " Arial"
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.fillText("Scorecards", 150, 35)
    const scorecardRowStart = 25
    const scoreCardTextEnd = 200

    const lineXStart = scorecardRowStart
    const lineXEnd = cardSection.end.x
    ctx.font = "bold " + FONT_SIZE.DEFAULT + " Arial"
    if (canvas.element.width < 768) {
        rowHeight = canvas.element.height * 0.5 / CANVAS_ROWS
        ctx.font = FONT_SIZE.DEFAULT + " Arial"
    }
    const lineY = rowHeight
    const gapAfterRow = 6
   
    ctx.fillText("Ones [5]", scorecardRowStart, rowHeight * 4)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.ONES, yatzyTable.value.players[i])
        //50 is just a test number
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 4}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 4 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 4 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Twos [10]", scorecardRowStart, rowHeight * 5)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.TWOS, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 5}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 5 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 5 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Threes [15]", scorecardRowStart, rowHeight * 6)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.THREES, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 6}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 6 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 6 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Fours [20]", scorecardRowStart, rowHeight * 7)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.FOURS, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 7}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 7 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 7 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Fives [25]", scorecardRowStart, rowHeight * 8)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.FIVES, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 8}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 8 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 8 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Sixes[30]", scorecardRowStart, rowHeight * 9)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.FIVES, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 9}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 9 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 9 + gapAfterRow)
    ctx.stroke()
    //ctx.font = "bold " +FONT_SIZE.DEFAULT+ " Arial"
    ctx.fillText("Subtotal [105]", scorecardRowStart, rowHeight * 10)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = {value: getPlayerTotal(yatzyTable.value.players[i]), handType:undefined}
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 10}
        fillPlayerScore(hand, startPoint)
    }
    ctx.moveTo(lineXStart, lineY * 10 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 10 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Bonus [50] (upper > 63)", scorecardRowStart, rowHeight * 11)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = {value: getPlayerBonus(yatzyTable.value.players[i]), handType:undefined}
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 11}
        fillPlayerScore(hand, startPoint)
    }
    ctx.moveTo(lineXStart, lineY * 11 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 11 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Pair [12]", scorecardRowStart, rowHeight * 12)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.PAIR, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 12}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 12 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 12 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Two pairs [22]", scorecardRowStart, rowHeight * 13)
     for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.TWO_PAIR, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 13}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 13 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 13 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Trips [18]", scorecardRowStart, rowHeight * 14)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.TRIPS, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 14}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 14 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 14 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Full house [28]", scorecardRowStart, rowHeight * 15)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.FULL_HOUSE, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 15}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 15 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 15 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Low straight (1-5) [15]", scorecardRowStart, rowHeight * 16)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.SMALL_STRAIGHT, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 16}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 16 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 16 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("High straight (2-6) [20]", scorecardRowStart, rowHeight * 17)
     for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.LARGE_STRAIGHT, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 17}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 17 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 17 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Quads [24]", scorecardRowStart, rowHeight * 18)
     for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.QUADS, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 18}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 18 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 18 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Chance [30]", scorecardRowStart, rowHeight * 19)
     for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.CHANCE, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 19}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 19 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 19 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Yatzy [50]", scorecardRowStart, rowHeight * 20)
     for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = getPlayerHand(HandType.YATZY, yatzyTable.value.players[i])
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 20}
        fillPlayerScore(hand, startPoint)        
    }
    ctx.moveTo(lineXStart, lineY * 20 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 20 + gapAfterRow)
    ctx.stroke()
    ctx.font = "bold " + FONT_SIZE.LARGER + " Arial"
    ctx.fillText("Grand Total [374]", scorecardRowStart, rowHeight * 21)
    for (let i = 0; i< yatzyTable.value.players.length; i++){
        const hand:IHand = {value: getPlayerTotal(yatzyTable.value.players[i]), handType:undefined}
        const startPoint:IVector2 = {x:scoreCardTextEnd  +50 + magic * i , y:rowHeight * 21}
        fillPlayerScore(hand, startPoint)
    }
    ctx.font = " " + FONT_SIZE.DEFAULT + " Arial"

    //Vertical lines
    for (let i = 0; i < yatzyTable.value.players.length; i++) {
        ctx.fillText(yatzyTable.value.players[i].name, scoreCardTextEnd + 20 + i * magic, rowHeight * 3)
        ctx.moveTo(scoreCardTextEnd + i * magic, rowHeight * 3)
        ctx.lineTo(scoreCardTextEnd + i * magic, rowHeight * 21)
        ctx.stroke()
    }
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
