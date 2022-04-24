<template>
	<div v-if="!showThanks && !loadingData" class="row">
		<div class="col fw-bold">     		
			Have some feedback? Are games not working, got some improvement ideas?
    	</div>
		<div class="row mt-4">
			<div class="col ">			
				
				<div>
					<textarea v-model="message" maxlength="400" class="form-control" rows="3"> </textarea>
					<button :disabled="!sendButtonEnabled" @click="send" class="btn btn-primary">
						Send feedback
					</button>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col">
				Known issues for 8-ball game: <br>
				1. On some occasions the ball to be collided with cue ball can go/move to an unexpected direction. <br>
				2. A ball can be wobbly in the cases where it goes along the pocket pathway. <br>
				3. The table view can be distorted in some occasions where multiple balls (and cue ball) go into pocket at the same time.<br>
				In these cases going to another browser window/tab and coming back refreshes the browser to the latest server level.<br>
			</div>
		</div>
		<div v-if="showError" class="row d-inline-flex p-2 alert-danger">		
				Error occured!
		</div>		
	</div>
	<div v-else-if="loadingData" class="row spinner-border" role="status">		
			<span class="sr-only"></span>			
	</div>
	<div v-if="showThanks" class="row mt-4 alert alert-success">
		<div class="col ">			
			Thanks!
		</div>
	</div>	
</template>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
	name: "Feedback",
	data(){
		return{
			message:"",
			error:false,
			success:false,
			loading:false
		}
	},
	computed:{
		sendButtonEnabled(){
				const message= this.message.trim()
				return message.length > 4
		},
		showError(){
			return this.error;
		},
		showThanks(){
			return this.success
		},
		loadingData(){
			return this.loading
		}
	},
	methods: {
		send(){
			console.log(process.env.VUE_APP_API_BASE_URL)
			const apiURL=process.env.VUE_APP_API_BASE_URL+"/portal/api/feedback"
			 const requestOptions = {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: null
                },
                body: JSON.stringify(this.message)
            }
			this.loading=true
            fetch(apiURL, requestOptions)
					.then(response => {				
						
						if (response.status !== 200) {
							return Promise.reject("error in feedback");
						}
						return
					})
					.then(
						data => {
							this.error = false;
							this.success=true
							this.message=""
						},
						(err) => {							
							this.error = true;
							this.success=false
						}
				).finally(() => {
					this.loading=false
				});		
		},	
	},
	created(){
		
	}
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
div.question{
	font-size:1.25rem;
}
</style>
