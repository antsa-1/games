<template>	
	<div class="col-xs-12 col-sm-4 mt-3 mt-md-0 pt-3">
			<span class="fw-bold ">
				{{title}}
			</span>		
			<ul class="list-group ">						
				<li v-for="(topPlayer, index) in games" :key="topPlayer" class="list-group-item" :class="[index%2==0?'bg-success p-2 text-dark bg-opacity-25':'bg-success p-2 text-dark bg-opacity-10']">					
					 <router-link :to="{ name: 'Profile', params:{selectedName:topPlayer.nickname}}" class="games-profile-link float-start">{{index+1}}.{{topPlayer.nickname}} </router-link>
					<span class="float-end"> {{getRanking(topPlayer)}}</span>
				</li>
			</ul>
			Games {{totalGames}}
		</div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { IUser,ITopLists,IPlayerStats} from "../../interfaces/interfaces";
export default {
	name: "Gamers",
 	props:{
    	games:Array,
    	title:String,
		totalGames:Number,
		gameNumber:Number
  	},
	data() {
		return {

		}
	},
	computed: {
		
	},

	created() {
		
	},

	methods: {
		getRanking(topPlayer:IPlayerStats){			
			if(this.gameNumber === 3){
				return topPlayer.rankingEightBall
			}
			if(this.gameNumber === 2){
				return topPlayer.rankingConnectFour
			}
			if(this.gameNumber === 1){
				return topPlayer.rankingTicTacToe
			}
		}
	}

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
