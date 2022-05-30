<template>	
	<div class="row mt-3">
		<p>
			<span class="fw-bold "> Hall of fame </span>
			<br>
			{{dateTime}}
		</p>
		
		<Gamers :games="eightBalls" :totalGames="totalEightBalls" :gameNumber="3" title="Eight ball" />
		<Gamers :games="connectFours" :totalGames="totalConnectFours" :gameNumber="2" title="ConnectFour" />
		<Gamers :games="tictactoes" :totalGames="totalTictactoes" :gameNumber="1" title="TicTacToe" />		
		
	</div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { ITopLists } from "../../interfaces/interfaces";
import Gamers from "./Gamers.vue";
export default defineComponent({
	name: "TopLists",
	components:{
		Gamers
	},
	data():ITopLists {
		return {			
			connectFours: [],
			tictactoes: [],
			eightBalls: [],
			errorFlag: false,
			dateTime: undefined,
			totalConnectFours: 0,
			totalTictactoes: 0,
			totalEightBalls: 0
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
			const apiURL = import.meta.env.VITE_APP_API_BASE_URL+"/portal/api/toplists"
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
						this.eightBalls = data.eightBalls
						this.dateTime = new Date(data.instant).toLocaleString()
						this.totalConnectFours = data.totalConnectFours
						this.totalTictactoes = data.totalTictactoes
						this.totalEightBalls = data.totalEightBalls
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
