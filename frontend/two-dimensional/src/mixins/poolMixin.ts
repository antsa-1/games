import { IUser } from "@/interfaces/interfaces";
import { IVector2,IBall, ICue, IPoolComponent } from "@/interfaces/pool";

import { loginMixin, ANONYM } from "./mixins";
import { tablesMixin } from "./tablesMixin";
const anonym_hanballs=[{x:261,y:352},{x:537,y:231}]
const olav_hanballs=[{x:401,y:301},{x:401,y:301}]
const anonym_turns=[{x:250,y:-0.013960698528745145},{x:250,y:-2.1070766760375603},{x:250,y:-1.4909663410826592}]
const olav_turns=[{x:225,y:0.44909863562584823},{x:225,y:0.44909863562584823}]

let  anohand_counter = 0
let  anoturn_counter = 0
let  olav_hand_counter = 0
let  olavturn_counter = 0
export const poolMixin = {
    mixins: [loginMixin, tablesMixin],
    data() {
		return {
			p:0x0
		};
	},
	computed: {
		

	},
	created() {
		

	},
	methods: {
		sendPocketSelection(pocketNumber:number){
			pocketNumber = pocketNumber === null? 0: pocketNumber
			const obj ={ title:"POOL_SELECT_POCKET", message: this.theTable.tableId, pool:{selectedPocket:pocketNumber}}
			console.log("*** Sending pocket selection to server"+JSON.stringify(obj))
			this.user.webSocket.send(JSON.stringify(obj))
		},
		hb(xc:number, yc:number, canvas){
			let position=null
			if(this.userName.startsWith("A")){
				position = anonym_hanballs[anohand_counter]
				anohand_counter++
			}else{
				position = olav_hanballs[olav_hand_counter]
				olav_hand_counter++
			}
		
			let cueBalla :IBall = <IBall> {position}
			const obj = { title:"POOL_HANDBALL", message: this.theTable.tableId, pool:{cueBall:cueBalla, canvas:{x:canvas.width, y:canvas.height}}}            
			console.log("*** Sending handball to server"+JSON.stringify(obj))
			this.user.webSocket.send(JSON.stringify(obj))
        },
		sendTurn(cue, cueBall, canvas){			
			const obj ={ title:"POOL_PLAY_TURN", message: this.theTable.tableId, pool:{ cue:this.prepareTransfer2(cue), cueBall:this.prepareTransfer(cueBall), canvas:{x:canvas.width, y:canvas.height}}}            
			console.log("*** Sending turn to server"+JSON.stringify(obj))
			this.user.webSocket.send(JSON.stringify(obj))
        },
        sp(cue, cueBall, canvas){
			//const obj ={ title:"POOL_UPDATE", message: this.theTable.tableId, pool:{ cue:this.prepareTransfer(cue), cueBall:this.prepareTransfer(cueBall), canvas:{x:canvas.width, y:canvas.height}}}
			//this.user.webSocket.send(JSON.stringify(obj))
        },

		prepareTransfer(object){
			const {image, color, ...object2} = object
		//	object2.angle= 0.0574905344833121;
		//	object2.force =250
		
			return object2
		},
		
		prepareTransfer2(object){
			const {image, color, ...object2} = object
		//	object2.angle= 0.0574905344833121;
		//	object2.force =250
		if(this.userName.startsWith("A")){
			let objc = anonym_turns[anoturn_counter]
			anoturn_counter++
			object2.force =objc.x
			object2.angle =objc.y
		}else{
			let objc = olav_turns[olavturn_counter]
			olavturn_counter++
			object2.force =objc.x
			object2.angle =objc.y
		}
			return object2
		},
		t(p, m, cue, cueBall, canvas, i = false){			
			if(this.p){				
				return
			}
			this.p =  0 <= 0x0
			setTimeout(()=> {
				p(cue, cueBall, canvas)
				this.p = (atob("dHJ1ZQ==") < "m" ) ? "YmFzZTY0IGlzIG5vIGVuY3J5cHRpb24gCg==" : 0.1 + 0.2 === 0.3
			}, m)
		},
		isHandBall(data){
			return data.pool.turnResult === "HANDBALL"
		},
		isOngoingGame(){
			if(!this.theTable){
				return false
			}
			if( this.theTable.playerInTurn){			
				return true
			}			
			return false
		},
		getClosestPocket(event:MouseEvent):number{
			if(event.offsetY < this.canvas.height / 2){
				if(event.offsetX < this.canvas.width /3){
					return 0
				}else if(event.offsetX < this.canvas.width * 0.66){
					return 1
				}
				return 2
			}
			if(event.offsetX < this.canvas.width /3){
				return 5
			}else if(event.offsetX < this.canvas.width * 0.66){
				return 4
			}
			return 3
		},		
	},
}