<template>
	<div class="row">
		<div class="col">			
			<button v-if="true" :disabled ="false" @click="" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Resign 
			</button>
			<button v-if="true" @click="" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Rematch
			</button>
		</div>
	</div>

	<div class="row">
		<div class="col-xs-12 col-sm-4">
			<label class="selectora" for="notificationSound" data-bs-toggle="tooltip" data-bs-placement="top" title="Beeps last 14 seconds in turn if checked">
				<i class="bi bi-music-note"></i>
			</label>
			<input  type="checkbox" id="notificationSound" v-model = "gameOptions.notificationSound">			
		</div>
	</div>	
    	<div class="row">
            {{yatzyTable}}
		<div class="col-xs-12 col-sm-4">
			<span v-if="yatzyTable" class="text-success">Playing turn </span>
			<span v-else-if="yatzyTable.playerInTurn?.name === userName" class="text-success"> It's your turn {{userName}} > time left:{{yatzyTable.secondsLeft}}  </span>
			<span v-else-if="yatzyTable?.playerInTurn === null" class="text-success"> Game ended </span>
			<div v-else class="text-danger"> In turn: {{yatzyTable.playerInTurn?.name}}</div>
		</div>
	</div>

	
	<div class="col-xs-12 col-sm-8">
	    <canvas id="canvas" width="1200" height="600" style="border:1px solid" ></canvas>
    </div>

	
</template>

<script setup lang="ts">

import { IYatzyComponent, IYatzyTable } from '@/interfaces/yatzy';
import { ref, computed, onMounted, onBeforeMount, onUpdated, watch, } from 'vue'
import { useRouter} from 'vue-router'
import { useStore} from 'vuex'  
import {IGameOptions, Image, IVector2, IGameCanvas, FONT_SIZE, FONT} from "../../interfaces/commontypes"  
import {IYatzyPlayer,IHand} from "../../interfaces/yatzy"  
import Chat from "../Chat.vue"
const CANVAS_ROWS = 22

    onMounted(() => {
        console.log("OnMounted")   
        let canvasElement = <HTMLCanvasElement> document.getElementById("canvas")      
        yatzyTable.value.canvas = <IGameCanvas>{element:canvasElement, animating:false, renderingContext:canvasElement.getContext("2d") }
        console.log("Canvas:"+yatzyTable.value.canvas)
        repaintYatzyTable()
    })
    const router = useRouter()
    const store = useStore()
    const gameOptions = ref<IGameOptions>({notificationSound:false})     
    const initTable = ():IYatzyTable => {
        let tablee:IYatzyTable = <IYatzyTable> store.getters.theTable
        const size: IVector2 = {x: 1200, y: 600}        
        const tableImage = <Image> {
            image: <HTMLImageElement>document.getElementById("yatzybg"), 
								canvasDimension: size,
								realDimension: {x:1024 ,y: 1024},
								canvasRotationAngle: { x:0, y:0 },
								visible: true
		}
        const players:IYatzyPlayer[] = tablee.players
        tablee.players.forEach(player => {
            player.dices = []
            player.scoreCard = {subTotal:0, total:0, bonus:0, hands:[], position:{x:0, y:0}}
        })
       
        tablee.image = tableImage
        tablee.players = players
        
        tablee.position = <IVector2> {x:0, y:0}
        return tablee
    }
    let yatzyTable = ref<IYatzyTable>(initTable())
    const userName = computed<string>(() => store.getters.user?.name)
    const selectedTextSize = () =>{
      
    }
    const unsubscribe = store.subscribe((mutation, state) => {
	    if (mutation.type === "rematch" ){				
	        console.log("rematch sub")
		}
		if (mutation.type === "resign" ){				
			console.log("resign sub")
		}
    })

    const unsubscribeAction = store.subscribeAction((action, state) => {
		console.log("action sub")
    })

    const repaintComponent = (component:IYatzyComponent) => {
        console.log("Repaint"+JSON.stringify(component))
        if(!component){
            return
        }
        const ctx = yatzyTable.value.canvas.renderingContext
        if( !component.image.visible){
		    return
		}
		ctx.translate(component.position.x, component.position.y)
		if(component.image.canvasRotationAngle){
		    ctx.rotate(component.image.canvasRotationAngle)
		}
		//void ctx.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
		if(!component.image.canvasDestination){
			component.image.canvasDestination = <IVector2> {x:0, y:0}
		}
        ctx.drawImage(component.image.image, 0, 0, component.image.realDimension.x, component.image.realDimension.y, component.image.canvasDestination.x, component.image.canvasDestination.y, component.image.canvasDimension.x, component.image.canvasDimension.y)			
		ctx.resetTransform()
    }
    const repaintScoreCard = () => {
        const canvas = yatzyTable.value.canvas
        const rowHeight = canvas.element.height *0.9 / CANVAS_ROWS
        const width =  canvas.element.width
        const ctx = canvas.renderingContext
        ctx.font = "bolder "+FONT_SIZE.LARGEST+" Arial"
        const scorecardRowStart = 25
		const scoreCardTextEnd = 200
     
        const lineXStart = scorecardRowStart 
        const lineXEnd = scoreCardTextEnd + yatzyTable.value.players.length * 100

        const lineY = rowHeight
        const gapAfterRow = 6
        ctx.lineWidth = 1;
        ctx.beginPath();

	    ctx.fillText("Scorecards", 150, 35)
        ctx.font = "bold " +FONT_SIZE.DEFAULT+ " Arial"
	    ctx.fillText("Ones [5]", scorecardRowStart, rowHeight * 4 )
        ctx.moveTo(lineXStart, lineY * 4 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 4 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Twos [10]", scorecardRowStart, rowHeight * 5 )
        ctx.moveTo(lineXStart, lineY * 5 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 5 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Threes [15]", scorecardRowStart, rowHeight * 6 )
        ctx.moveTo(lineXStart, lineY * 6 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 6 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Fours [20]", scorecardRowStart, rowHeight * 7 )
        ctx.moveTo(lineXStart, lineY * 7 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 7 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Fives [25]", scorecardRowStart, rowHeight * 8 )
        ctx.moveTo(lineXStart, lineY * 8 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 8 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Sixes[30]", scorecardRowStart,rowHeight * 9 )
        ctx.moveTo(lineXStart, lineY * 9 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 9 + gapAfterRow)
        ctx.stroke()
        //ctx.font = "bold " +FONT_SIZE.DEFAULT+ " Arial"
        ctx.fillText("Subtotal [105]", scorecardRowStart, rowHeight * 10 )
        ctx.moveTo(lineXStart, lineY * 10 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 10 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Bonus [50] (upper > 63)", scorecardRowStart, rowHeight * 11 )
        ctx.moveTo(lineXStart, lineY * 11 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 11 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Pair [12]", scorecardRowStart, rowHeight * 12 )
        ctx.moveTo(lineXStart, lineY * 12 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 12 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Two pairs [22]", scorecardRowStart, rowHeight * 13)
        ctx.moveTo(lineXStart, lineY * 13 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 13 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Trips [18]", scorecardRowStart, rowHeight * 14 )
        ctx.moveTo(lineXStart, lineY * 14 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 14 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Full house [28]", scorecardRowStart, rowHeight * 15 )
        ctx.moveTo(lineXStart, lineY * 15 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 15 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("Low straight (1-5) [15]", scorecardRowStart, rowHeight * 16)
        ctx.moveTo(lineXStart, lineY * 16 + gapAfterRow)
		ctx.lineTo(lineXEnd, lineY * 16 + gapAfterRow)
        ctx.stroke()
        ctx.fillText("High straight (2-6) [20]", scorecardRowStart, rowHeight * 17 )
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
        ctx.font = " " +FONT_SIZE.DEFAULT+ " Arial" 
        for(let i = 0; i<yatzyTable.value.players.length; i++){
            ctx.fillText(yatzyTable.value.players[i].name, scoreCardTextEnd + 20 + i * 120, rowHeight * 3)
            ctx.moveTo(scoreCardTextEnd + i * 120, rowHeight * 3)
		    ctx.lineTo(scoreCardTextEnd + i * 120, rowHeight * 21)
            ctx.stroke()
        }
	    ctx.closePath()
    }

    const repaintYatzyTable = () => {
        console.log("RepaintAll")
        repaintComponent (yatzyTable.value)
        repaintScoreCard()
        
    }
   

  
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<!--
 * 	@author antsa-1 from GitHub 
 -->
<style scoped>
.hidden{
	visibility:hidden
}
</style>
