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


    <div >
        <canvas id="canvas" width="1200" height="600" style="border:1px solid"></canvas>
    </div>


</template>

<script setup lang="ts">

import { IUser } from '@/interfaces/interfaces';
import { IYatzyComponent, IYatzyTable } from '@/interfaces/yatzy';
import { ref, computed, onMounted, onBeforeMount, onUnmounted, watch, } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { IGameOptions, Image, IVector2, IGameCanvas, FONT_SIZE, FONT } from "../../interfaces/commontypes"
import { IYatzyPlayer, IHand,IDice,ISection, } from "../../interfaces/yatzy"
import Chat from "../Chat.vue"
const CANVAS_ROWS = 22


onMounted(() => {
    console.log("OnMounted")
    let canvasElement = <HTMLCanvasElement>document.getElementById("canvas")
    yatzyTable.value.canvas = <IGameCanvas>{ element: canvasElement, animating: false, ctx: canvasElement.getContext("2d") }
    yatzyTable.value.canvas.ctx.imageSmoothingEnabled = true
    attachListeners()
    repaintYatzyTable()
})

onUnmounted(() => {
    console.log("onUnmounted")
    //unsubscribe()
	//unsubscribeAction()
	//learIntervals()
	//removeMouseListeners()
		//window.removeEventListener("resize", this.resize)
	//document.removeEventListener("visibilitychange", this.onVisibilityChange)
	const obj = { title: "LEAVE_TABLE", message: tablee.tableId }
    store.getters.user.webSocket?.send(JSON.stringify(obj))
    store.dispatch("clearTable")
})
const router = useRouter()
const store = useStore()
const gameOptions = ref<IGameOptions>({ notificationSound: false })

const resizeDocument = () => {
    console.log("resizeEvent"+screen.width )
    if (screen.width >= 1200) {
        yatzyTable.value.canvas.element.width = 1100
        yatzyTable.value.canvas.element.height = screen.height
        yatzyTable.value.image.canvasDimension.x = 1100
        yatzyTable.value.image.canvasDimension.y = screen.height
    }
    else if (screen.width >= 768) {
        yatzyTable.value.canvas.element.width = screen.width *0.9
        yatzyTable.value.canvas.element.height = screen.height
        yatzyTable.value.image.canvasDimension.x = screen.width *0.9
        yatzyTable.value.image.canvasDimension.y = screen.height
    }
    else if (screen.width >= 600) {
        yatzyTable.value.canvas.element.width = 600
        yatzyTable.value.canvas.element.height = 800
        yatzyTable.value.image.canvasDimension.x = 600
        yatzyTable.value.image.canvasDimension.y = 800
    }
    
    repaintYatzyTable()
}

const playTurn = () => {
    let rollDices  =[]
    console.log("DIces:"+JSON.stringify(tablee.dices))
    debugger
    for (let i = 0; i < tablee.dices.length; i++){
        const {diceId,locked} = tablee.dices[i];
        rollDices.push({diceId:diceId,locked:locked})
    }
    const obj = { title:"YATZY_ROLL_DICES", message: tablee.tableId, yatzy:{dices:rollDices}}
	console.log("*** Sending dices "+JSON.stringify(obj))
	user.value.webSocket.send(JSON.stringify(obj))
}

const attachListeners = () => {
    window.addEventListener('resize', resizeDocument)
    console.log("Canvas:" + yatzyTable.value.canvas)
    const canvas = document.getElementById("canvas")
    canvas.addEventListener('pointerover', (event) => {
        console.log('Pointer moved in')
    })
    canvas.addEventListener('pointerdown', (event) => {
        const cursorPoint = {x:event.offsetX, y: event.offsetY}
        console.log('Pointer down event')
        const dice:IDice = getDice(cursorPoint)
        if(dice && !dice.locked){
            dice.selected? dice.selected = false : dice.selected = true
        }else if(isPointOnImage(cursorPoint, yatzyTable.value.playButton.image)){
            playTurn()  
        }
        repaintYatzyTable()
    })
    canvas.addEventListener('pointermove', (event) => {
        const cursorPoint = {x:event.offsetX, y: event.offsetY}
        document.body.style.cursor = "default"
        if(isPointOnSection(cursorPoint, diceSection()) ){
            const dice:IDice = getDice(cursorPoint)
            if(dice){
                document.body.style.cursor = "pointer"               
                repaintYatzyTable()         
            }
        }else if(isPointOnSection(cursorPoint, buttonSection())){
            if(isPointOnImage(cursorPoint, yatzyTable.value.playButton.image)){
                document.body.style.cursor = "pointer"               
                repaintYatzyTable() 
            }
        }
                    
    })
   // canvas.addEventListener("pointerdown", this.handleMouseDown, false)
  //	canvas.addEventListener("pointerup", this.handleMouseUp, false)
 	//canvas.addEventListener("pointermove", this.handleMouseMove, false)
	canvas.addEventListener("contextmenu",(e)=> e.preventDefault())
}

const getDice = (cursorPoint:IVector2) => {
   return yatzyTable.value.dices.filter(dice => filter(dice, cursorPoint ))[0]
}

const filter = (dice:IDice , cursorPoint:IVector2) => {
    const size = diceSize ()
    if(cursorPoint.x < dice.position.x ){
      //  console.log("failed1")
        return false
    }
    if(cursorPoint.x > dice.position.x + size.x){
      //  console.log("failed22")
        return false
    }
    if(cursorPoint.y < dice.position.y){
      //  console.log("failed3")
        return false
    }
    if(cursorPoint.y > dice.position.y+ size.y){
        return false
    }  
    return true
}

const isPointOnImage = (point:IVector2, image:Image) => {
   
    if(point.x < image.canvasDestination.x || point.x > image.canvasDestination.x + image.canvasDimension.x ){
        return false
    }
    if(point.y < image.canvasDestination.y || point.y > image.canvasDestination.y + image.canvasDimension.y ){
        return false
    }
    return true
}

const isPointOnSection = (point:IVector2, section:ISection) => {  
  
    if(point.x < section.start.x || point.x > section.end.x ){    
        
        return false;
    }
    if(point.y < section.start.y || point.y > section.end.y ){   
       
        return false;
    }
   
    return true
}

let tablee: IYatzyTable = <IYatzyTable>store.getters.theTable
const initTable = (): IYatzyTable => {
    console.log("initTable:")
    
    const bgSize: IVector2 = { x: 1200, y: 1200 }
    const tableImage = <Image>{
        image: <HTMLImageElement>document.getElementById("yatzybg"),
        canvasDimension: bgSize,
        canvasDestination:<IVector2>{x:0, y:0},
        realDimension: { x: 1024, y: 1024 },
        canvasRotationAngle: 0,
        visible: true
    }
    const players: IYatzyPlayer[] = tablee.players
    tablee.players.forEach(player => {    
        player.scoreCard = { subTotal: 0, total: 0, bonus: 0, hands: [], position: { x: 0, y: 0 } }
    })
   
    //console.log("tablee:"+JSON.stringify(tablee.dices))
    for( let i = 1; i < 6; i++){
        const diceImage = <Image>{
            image: <HTMLImageElement>document.getElementById("dice" +i),
            canvasDimension: { x: 80, y: 80 },
            realDimension: { x: 1024, y: 1024 },
            canvasRotationAngle: 0,
            canvasDestination:<IVector2>{x:0, y:0},
            visible: true
        }
        
       
        let dice:IDice = tablee.dices[i-1]
        dice.image = diceImage 
        
       // tablee.dices.push(dice)
    }
    console.log("dices:"+tablee.dices)
    tablee.image = tableImage
    tablee.players = players

    const playButtonImage =  <Image>{
            image: <HTMLImageElement>document.getElementById("playButton"),
            canvasDimension: {x:100, y:50},
            realDimension: { x: 570, y: 279 },
            canvasRotationAngle: 0,
            canvasDestination:<IVector2>{x:0, y:0},
            visible: true
    }
   
    tablee.playButton = {image:playButtonImage, position:{x:650, y:300}}
    tablee.position = <IVector2>{ x: 0, y: 0 }
    let playerInTurn:IYatzyPlayer =<IYatzyPlayer> tablee.playerInTurn
 
    return tablee
}

let yatzyTable = ref<IYatzyTable>(initTable())
const userName = computed<string>(() => store.getters.user?.name)
const user = computed<IUser>(() => store.getters.user)
const selectedTextSize = () => {

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
    console.log("action sub")
})

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
    let table:IYatzyPlayer = <IYatzyPlayer> yatzyTable.value.playerInTurn

}

const startTurnButtonVisible = () => {
    return userName.value === yatzyTable.value.playerInTurn.name
}
const repaintDices = () => {
    const section = diceSection ()

    for( let i = 0; i < 5; i++){       
        const dice = yatzyTable.value.dices[i]
        const size:IVector2 = diceSize()
        if(dice.selected){
            //position down size of one dice
            dice.position = {x: section.start.x + i * size.x + +60, y:section.start.y +size.y}
        } 
        else if(dice.locked){
            //locked dice position down of two two dice
            dice.position = {x: section.start.x + i * size.x+ 60, y:section.start.y + 2* size.y}
        }  
        else {
            //basic position
            dice.position = {x: section.start.x + i * size.x + 60, y:section.start.y}
        }
        dice.image.canvasDestination = dice.position
        dice.image.canvasDimension.x = size.x
        dice.image.canvasDimension.x = size.y
        repaintComponent(dice)
    }
}
let highlightedDice:IDice = null

const removeHighlightFromDice = ():boolean => {
    if(highlightDice){
        highlightedDice = null
        return true
    }
    return false
}
const highlightDice = (dice:IDice) => {
    if(highlightedDice && highlightedDice.diceId === dice.diceId){
        console.log("highlight does not change")
        return false
    }
    console.log("highlight changes")
    highlightedDice = dice
    return true
}

const repaintButtons = () => {
    const button = yatzyTable.value.playButton
    const section:ISection = buttonSection()
    button.position.x = section.end.x - button.image.canvasDimension.x
    button.position.y = section.end.y - button.image.canvasDimension.y
    button.image.canvasDestination = button.position
    if(userName.value === tablee.playerInTurn.name){
        repaintComponent(yatzyTable.value.playButton)
    }
}

const gameSection = ():ISection => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    const size = diceSize()
    if(canvasWidth >= 1200){
        const y = yatzyTable.value.canvas.element.height / 10   
        return {start: {x: 700, y:y}, end: {x:700+ 6 * size.x, y:y + (size.y * 2)}}
    }
    if(canvasWidth >= 768){
        const x = canvasWidth/2
        const y = 300
           console.log("bbb")
        return {start: {x:x, y:y},end:{x:x + 6 * size.x, y:y + size.y}}
    }
    const x = 220
    const y = 500
    if(canvasWidth > 600){
           console.log("ccc")
        return  {start: {x:x, y:y}, end:{x:x + 6 * size.x, y:y + size.y}}
    }
       console.log("ddd")
    return {start: {x:x, y:y}, end:{x:x+ 6 * size.x, y:y + size.y}}
}

const diceSection = ():ISection => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    console.log("diceSection canvasWidth:"+canvasWidth)
    const size:IVector2 = diceSize()
    if(canvasWidth >= 1200){
        const y = yatzyTable.value.canvas.element.height / 10   
        return {start: {x: 700, y:y}, end: {x:700+ 6 * size.x, y:y + (size.y * 2)}}
    }
    if(canvasWidth >= 768){
        const x = canvasWidth/2
        const y = 300
           console.log("bbb")
        return {start: {x:x, y:y},end:{x:x + 6 * size.x, y:y + size.y}}
    }
    const x = 220
    const y = 500
    if(canvasWidth > 600){
           console.log("ccc")
        return  {start: {x:x, y:y}, end:{x:x + 6 * size.x, y:y + size.y}}
    }
       console.log("ddd")
    return {start: {x:x, y:y}, end:{x:x+ 6 * size.x, y:y + size.y}}
}

const buttonSection = ():ISection => {
    //button section comes under dice section
    const dices:ISection = diceSection()
    const dSize = diceSize()
  
    
    const buttonSectionStart:IVector2 = {x: dices.start.x, y:dSize.y *2 }
    const buttonSectionEnd:IVector2 = {x: dices.end.x, y:dSize.y *3.5}
    return  {start:buttonSectionStart, end:buttonSectionEnd}
}

const diceSize = ():IVector2 => {
    const canvasWidth = yatzyTable.value.canvas.element.width
    if(canvasWidth > 1000){
        return {x:80, y:80}
    }
    if(canvasWidth >= 768){

        return {x:60, y:60}
    }
    return {x:40, y:40}
}

const repaintYatzyTable = () => {
    console.log("RepaintAll")
    const canvas = yatzyTable.value.canvas
    const ctx = canvas.ctx   
    ctx.clearRect(0, 0, canvas.element.width, canvas.element.height)
    repaintComponent(yatzyTable.value)
    repaintScoreCard()
    repaintDices()
    repaintButtons()
}

const repaintScoreCard = () => {
    const canvas = yatzyTable.value.canvas
    let rowHeight = canvas.element.height * 0.9 / CANVAS_ROWS
   
    const width = canvas.element.width
    const ctx = canvas.ctx
    ctx.font = "bolder " + FONT_SIZE.LARGEST + " Arial"
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.fillText("Scorecards", 150, 35)
    const scorecardRowStart = 25
    const scoreCardTextEnd = 200

    const lineXStart = scorecardRowStart
    const lineXEnd = scoreCardTextEnd + yatzyTable.value.players.length * 100
    ctx.font = "bold " + FONT_SIZE.DEFAULT + " Arial"
    if(canvas.element.width < 768){
        rowHeight = canvas.element.height * 0.5 / CANVAS_ROWS
        ctx.font = FONT_SIZE.DEFAULT + " Arial"
    }
    const lineY = rowHeight
    const gapAfterRow = 6

    ctx.fillText("Ones [5]", scorecardRowStart, rowHeight * 4)
    ctx.moveTo(lineXStart, lineY * 4 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 4 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Twos [10]", scorecardRowStart, rowHeight * 5)
    ctx.moveTo(lineXStart, lineY * 5 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 5 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Threes [15]", scorecardRowStart, rowHeight * 6)
    ctx.moveTo(lineXStart, lineY * 6 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 6 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Fours [20]", scorecardRowStart, rowHeight * 7)
    ctx.moveTo(lineXStart, lineY * 7 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 7 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Fives [25]", scorecardRowStart, rowHeight * 8)
    ctx.moveTo(lineXStart, lineY * 8 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 8 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Sixes[30]", scorecardRowStart, rowHeight * 9)
    ctx.moveTo(lineXStart, lineY * 9 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 9 + gapAfterRow)
    ctx.stroke()
    //ctx.font = "bold " +FONT_SIZE.DEFAULT+ " Arial"
    ctx.fillText("Subtotal [105]", scorecardRowStart, rowHeight * 10)
    ctx.moveTo(lineXStart, lineY * 10 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 10 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Bonus [50] (upper > 63)", scorecardRowStart, rowHeight * 11)
    ctx.moveTo(lineXStart, lineY * 11 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 11 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Pair [12]", scorecardRowStart, rowHeight * 12)
    ctx.moveTo(lineXStart, lineY * 12 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 12 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Two pairs [22]", scorecardRowStart, rowHeight * 13)
    ctx.moveTo(lineXStart, lineY * 13 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 13 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Trips [18]", scorecardRowStart, rowHeight * 14)
    ctx.moveTo(lineXStart, lineY * 14 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 14 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Full house [28]", scorecardRowStart, rowHeight * 15)
    ctx.moveTo(lineXStart, lineY * 15 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 15 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Low straight (1-5) [15]", scorecardRowStart, rowHeight * 16)
    ctx.moveTo(lineXStart, lineY * 16 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 16 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("High straight (2-6) [20]", scorecardRowStart, rowHeight * 17)
    ctx.moveTo(lineXStart, lineY * 17 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 17 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Quads [24]", scorecardRowStart, rowHeight * 18)
    ctx.moveTo(lineXStart, lineY * 18 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 18 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Chance [30]", scorecardRowStart, rowHeight * 19)
    ctx.moveTo(lineXStart, lineY * 19 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 19 + gapAfterRow)
    ctx.stroke()
    ctx.fillText("Yatzy [50]", scorecardRowStart, rowHeight * 20)
    ctx.moveTo(lineXStart, lineY * 20 + gapAfterRow)
    ctx.lineTo(lineXEnd, lineY * 20 + gapAfterRow)
    ctx.stroke()
    ctx.font = "bold " + FONT_SIZE.LARGER + " Arial"
    ctx.fillText("Grand Total [374]", scorecardRowStart, rowHeight * 21)
    ctx.font = " " + FONT_SIZE.DEFAULT + " Arial"
    for (let i = 0; i < yatzyTable.value.players.length; i++) {
        ctx.fillText(yatzyTable.value.players[i].name, scoreCardTextEnd + 20 + i * 120, rowHeight * 3)
        ctx.moveTo(scoreCardTextEnd + i * 120, rowHeight * 3)
        ctx.lineTo(scoreCardTextEnd + i * 120, rowHeight * 21)
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
.yatzyLeft{
    position:absolute;
    left:0px 
}
</style>
