import { IUser } from "@/interfaces/interfaces";
import { IVector2,IBall, ICue, IPoolComponent } from "@/interfaces/pool";

import { loginMixin, ANONYM } from "./mixins";
import { tablesMixin } from "./tablesMixin";

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
		hb(cueBall, canvas){			
			const obj ={ title:"POOL_HANDBALL", message: this.theTable.tableId, pool:{cueBall:this.prepareHandBall(cueBall), canvas:{x:canvas.width, y:canvas.height}}}            
			console.log("Handball send"+JSON.stringify(obj))
			this.user.webSocket.send(JSON.stringify(obj))
        },
		st(cue, cueBall, canvas){			
			const obj ={ title:"POOL_PLAY_TURN", message: this.theTable.tableId, pool:{ cue:this.prepareTransfer(cue), cueBall:this.prepareTransfer(cueBall), canvas:{x:canvas.width, y:canvas.height}}}            
			console.log("Play turn"+JSON.stringify(obj))
			this.user.webSocket.send(JSON.stringify(obj))
        },
        sp(cue, cueBall, canvas){
			const obj ={ title:"POOL_UPDATE", message: this.theTable.tableId, pool:{ cue:this.prepareTransfer(cue), cueBall:this.prepareTransfer(cueBall), canvas:{x:canvas.width, y:canvas.height}}}
			this.user.webSocket.send(JSON.stringify(obj))
        },
		prepareHandBall(object){
			const {image, color, ...object2} = object			
			return object2
		},
		//:0.060570977279457795,"visible":true},"force":190,"
		prepareTransfer(object){
			const {image, color, ...object2} = object
		//	object2.angle= 0.060570977279457795;
		//	object2.force =190
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
		checkHandBall(data){
			return () => {
				if(data.pool.turnResult === "HANDBALL"){
					console.log("handball")
					this.$store.dispatch("poolHandBall", data.pool)
				}
			}
		},
	},
}