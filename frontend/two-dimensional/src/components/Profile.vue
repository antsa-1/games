<template>

	<div v-if="memberSince" class="row">
		<div class="row col-md-6 offset-md-3 alert alert-info text-start">
			<b>{{selectedName}}  </b>
			<br>
			Joined: {{memberSince}}
			<br>		
			{{text}}
		</div>	
		<div class="row">
			<div v-if="connectFours.length>0" class="col-xs-12 col-sm-6">
				<GameHistory :games="connectFours" title="Connect fours" />
			</div>
			<div v-if="tictactoes.length>0" class="col-xs-12 col-sm-6">
				<GameHistory :games="tictactoes" title="TicTacToes" />
			</div>
		</div>
	</div>
	<div v-if="status==204 || status>=400" class="row">
		User not found
	</div>
	
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { loginMixin } from "../mixins/mixins";
import GameHistory from "./GameHistory.vue";
import { IProfile, IUser,GameResult } from "../interfaces/interfaces";
export default defineComponent({
	name: "Profile",
	mixins: [loginMixin],
	components:{
		GameHistory
	},
	props: {
		selectedName: String
	},
	data():IProfile{
		return {
			stats: null,
			text: null,
			memberSince:null,
			connectFours:[],
			tictactoes:[],
			status:-1,
			fetchText:''
		}
	},
	computed: {
	
	},

	created() {
		this.fetchProfile()
	},

	methods: {
		fetchProfile() {
			this.status=-1;
			this.fetchText=""		
			
			const requestOptions = {
				method: "GET",
				headers: {
					"Content-Type": "application/json",
					Authorization: null
				},
			
			}
			const apiURL=process.env.VUE_APP_API_BASE_URL+"/portal/api/profile/"+this.selectedName
			this.$store.dispatch("setLoadingStatus", true)
			fetch(apiURL, requestOptions)
				.then(response => {	
					this.status=response.status;			
					if (response.status !== 200) {
						return Promise.reject("error fetching profile");
					}
					return response.json();
				}).then(
					data => {
						this.text=data.user.profileText
						this.memberSince= new Date(data.user.memberSince).toLocaleDateString()
						this.tictactoes=data.user.tictactoes
						this.connectFours=data.user.connectFours						
					},
					(err) => {
						console.error("error in profile fetch")	
					}
				).finally(() => {				
					this.$store.dispatch("setLoadingStatus", false)
				});
		},
	
		
		},
	
});
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>


