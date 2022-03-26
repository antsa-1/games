import { IUser } from "@/interfaces/interfaces";
import { IVector2,IBall } from "@/interfaces/pool";

import { loginMixin, ANONYM } from "./mixins";
import { tablesMixin } from "./tablesMixin";

export const poolMixin = {
    mixins: [loginMixin, tablesMixin],
    data() {
		return {
			token: null
		};
	},
	computed: {
		

	},
	created() {
		

	},
	methods: {
		playTurn(cueBall:IBall, canvas:HTMLCanvasElement){
            const obj ={ title:"POOL_MOVE", message: this.theTable.tableId, velocity:cueBall.velocity, cueBall:cueBall.position, canvasWidth:canvas.width, canvasHeight: canvas.height}
            console.log("poolMixin sending pool move"+ JSON.stringify(obj, null , 2))
			//this.user.webSocket.send(JSON.stringify(obj))
        },
        sendPoolCueUpdate(rotationAngle: IVector2, cueBall:IBall, canvas:HTMLCanvasElement){ // percentage??
            const obj ={ title:"POOL_CUE_UPDATE", message: this.theTable.tableId, pool:{ cueBallPosition:cueBall.position, cueAngle:rotationAngle, canvas:{x:canvas.width, y:canvas.height} }}
          //  console.log("poolMixin sending cueUpdate"+ JSON.stringify(obj, null , 2))
		//	this.user.webSocket.send(JSON.stringify(obj))
        },
     
	},
}