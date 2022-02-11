<template>
	<div class="row mb-3 mt-2">
		<div class="col ">
			<span class="fw-bold">
				<h3>Terms and conditions 09.02.2022</h3>
			</span>
			<p class="">
				<span class=" fw-bold">Cookies</span>
				<br>
				This site does not use cookies to store information. Instead it uses browser's session storage to store login token.<br>
				With basic browser settings token is removed after closing or after using logging out functionality.
				 <br>
			</p>	
			<p class="">
				<span class=" fw-bold">Browsers</span>
				<br>
				 Recommended browsers are latest versions of Firefox, Chrome and Edge.
				 <br>
			</p>
			<p class="">
				<span class=" fw-bold">Intention of the site</span>
				<br>
				 Play TicTacToe and Connect 4 games.
			</p>
			<p class="">
				<span class=" fw-bold">Bugs</span>
				<br>
				No bounties are available for bugs. But if you track them and find somethings please use the feedback form for informing.
				<br>
				Frontend and backend source codes are available from https://github.com/antsa-1/games 
			</p>	
		</div>
	</div>		
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { IChat } from "../interfaces";
export default defineComponent({
	name: "Info",

	data(){
		return {			
			messages:null,
			message:{text:""},
		}
	},
	computed:{
	
		messageExist() {
			return this.message.text.length > 0
		},
		
	},
	created(){
		
	},
	methods: {
		sendMessage(){
			let message:string=this.message.text.trim()
			if(message.length<1 || message.length >200){				
				return
			}
			
		}	
	},
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
