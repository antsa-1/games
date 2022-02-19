<template>
	
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { IGameToken, IUser } from "../interfaces";
import { loginMixin } from "../mixins/mixins";
import { useRoute } from 'vue-router';
interface GameChannel {
	user: IUser;
	lobbyText: string;
}

export default defineComponent({
	name: "Websocket",
	props: {
		token: String
	},
	mixins: [loginMixin],

	data(): GameChannel {
		return {
			user: {name:""},
			lobbyText: "qq"
		};
	},
	computed:{
		usera(){
			return this.$store.getters.user
		}
	},

	created() {
	
		
	},
	methods: {
		connect(token:string): void {
		
			this.usera.webSocket = new WebSocket("ws://localhost:8081/tictactoe/ws");
		
			this.usera.webSocket.onopen = event => {
			
				this.authenticateSocket(token);
			};
			this.usera.webSocket.onmessage = event => {
				let data = JSON.parse(event.data);
				
			
				switch (data.title) {
					case "LOGIN":
					
						this.login(data.message)
						
						break;
					case "username":
						break;
				}
			};
			this.usera.webSocket.onerror = event => {
				
			};
			this.usera.webSocket.onclose = event => {
				
			};
		},

		authenticateSocket(token:string): void {
			
			let title2 = "LOGIN";
			

			if (this.usera) {
				const obj = { title: title2, message: token };
				const myJSON = JSON.stringify(obj);
				let combinedMessage = JSON.stringify(obj);
				// const messageJSON = JSON.parse(combinedMessage)

				this.usera.webSocket.send(combinedMessage);
			} else {
				alert("no connection");
			}
		}
	}
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
