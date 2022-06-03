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
			<span v-if="yatzyTable.mouseEnabled" class="text-success">Playing turn {{yatzyTable.mouseEnabled}}</span>
			<span v-else-if="yatzyTable.playerInTurn?.name === userName" class="text-success"> It's your turn {{userName}} > time left:{{yatzyTable.secondsLeft}}  </span>
			<span v-else-if="yatzyTable?.playerInTurn === null" class="text-success"> Game ended </span>
			<div v-else class="text-danger"> In turn: {{yatzyTable.playerInTurn?.name}}</div>
		</div>
	</div>

	
	<div class="col-xs-12 col-sm-8">
	    <canvas id="canvas" width="1200" height="600" style="border:1px solid" ></canvas>
    </div>

	
</template>

<script setup  lang="ts">
import { IYatzyComponent, IYatzyTable } from '@/interfaces/yatzy';
import { ref, computed, onMounted, onBeforeMount, onUpdated, watch, } from 'vue'
import { useRouter} from 'vue-router'
import { useStore} from 'vuex'  
import {IGameOptions, Image, IVector2, IGameCanvas} from "../../interfaces/commontypes"  
import {IYatzyPlayer,IHand} from "../../interfaces/yatzy"  
import Chat from "../Chat.vue"
    onMounted(() => {
        console.log("OnMounted")   
        let canvasElement = <HTMLCanvasElement> document.getElementById("canvas")      
        yatzyTable.value.canvas = <IGameCanvas>{element:canvasElement, animating:false, renderingContext:canvasElement.getContext("2d") }
        console.log("Canvas:"+yatzyTable.value.canvas)
        repaintAll()
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

    const repaintComponent = (component:IYatzyComponent) =>{
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

    const repaintAll = () => {
        console.log("RepaintAll")
        repaintComponent (yatzyTable.value)
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
