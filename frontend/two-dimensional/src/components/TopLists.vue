<template>	
	<div class="row mt-3">
		<span class="fw-bold "> Hall of fame </span>
		<div class="col-xs-12 col-sm-4 mt-3 mt-md-0 pt-3">
			<span class="fw-bold ">
				Connect Four
			</span>		
			<ul class="list-group ">						
				<li v-for="(topPlayer, index) in connectFours" :key="topPlayer" class="list-group-item" :class="[index%2==0?'bg-success p-2 text-dark bg-opacity-25':'bg-success p-2 text-dark bg-opacity-10']">					
					<div class="float-start ">
						{{index+1}}.{{topPlayer.nickname}} 					
					</div>
					<span class="float-end"> {{topPlayer.rankingConnectFour}}</span>	
				</li>
			</ul>
		</div>
		<div class="col-xs-12 col-sm-4 mt-3 mt-md-0 pt-3">
			<span class="fw-bold ">
				TicTacToe
			</span>		
			<ul class="list-group ">						
				<li v-for="(topPlayer, index) in tictactoes" :key="topPlayer" class="list-group-item" :class="[index%2==0?'bg-success p-2 text-dark bg-opacity-25':'bg-success p-2 text-dark bg-opacity-10']">					
					<div class="float-start ">
						{{index+1}}.{{topPlayer.nickname}} 					
					</div>
					<span class="float-end"> {{topPlayer.rankingTicTacToe}}</span>
				</li>
			</ul>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { IUser,ITopLists } from "../interfaces";
export default defineComponent({
	name: "TopLists",

	data():ITopLists {
		return {			
			connectFours:[],
			tictactoes:[],
			errorFlag:false			
		};
	},
	computed: {		
		
	},

	created() {
		this.fetchLists()
	},

	methods: {
		fetchLists() {
			console.log("Ui starts fetch")			
			this.errorFlag = false	
			const requestOptions = {
				method: "GET",
				headers: {
					"Content-Type": "application/json",
					Authorization: null
				},
			
			}
			const apiURL=process.env.VUE_APP_API_BASE_URL+"/portal/api/toplists"
			this.$store.dispatch("setLoadingStatus", true)
			fetch(apiURL, requestOptions)
				.then(response => {					
					if (response.status !== 200) {
						return Promise.reject("error fetching lists");
					}
					return response.json();
				}).then(
					data => {						
						this.connectFours = data.connectFours
						this.tictactoes  = data.tictactoes
					},
					(err) => {
						this.errorFlag = true			
					}
				).finally(() => {				
					this.$store.dispatch("setLoadingStatus", false)
				});
		},
	}

});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
