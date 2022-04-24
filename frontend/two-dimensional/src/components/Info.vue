<template>
	<div class="row mb-3 mt-2">
		<div class="col ">
			<span class="fw-bold">
				<h3>Terms and conditions 24.02.2022</h3>
			</span>
			<p class="">
				<span class=" fw-bold">General</span>
				<br>
				 Site is under construction and can change/reset data without informing. At the moment you can play eightball, tic-tac-toe and connect4 games.
				 On some occasions IP-address of the client is logged.
				 <br>
			</p>
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
				<span class=" fw-bold">Bugs </span>
				<br>
				No bounties are available for bugs. But if you track them and find anything please use the feedback form for notifying.		
			</p>
			<p class="">
				<span class=" fw-bold">Disclaimer</span>
				<br>
				You use this site at your own risk.<br>
				There is no guarantee for this website to work error free. The author of this site is not liable for any damage what could be caused by using this website.<br>
			</p>
		</div>
	</div>		
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { IChat } from "../interfaces/interfaces";
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
