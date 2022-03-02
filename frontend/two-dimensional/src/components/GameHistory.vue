<template>
  <span class="fw-bold ">
			{{title}} 
		</span>		
		<ul class="list-group ">						
			<li v-for="(game, index) in games" :key="game" class="list-group-item" :class="[index%2==0?'bg-success p-2 text-dark bg-opacity-25':'bg-success p-2 text-dark bg-opacity-10']">					
				<div class="">
					{{convertInstantToDate (game.startInstant)}} 
					<br>
					{{game.playerAName}} - {{game.playerBName}}
					<br>
					{{game.playerAStartRanking}}  ( {{getRankingChange(game.playerAEndRanking,game.playerAStartRanking)}}  ) vs. {{game.playerBStartRanking}} ( {{getRankingChange(game.playerBEndRanking, game.playerBStartRanking)}} )
					<br>
						Board: {{game.boardDescription}}
					<p v-if="game?.winnerUsername">
						Winner: {{game.winnerUsername}}
						<br>
						{{convertResultToText(game.result)}}
					</p>
					<p v-else>
						 {{convertResultToText(game.result)}}
					</p>				
										
				</div>						
			</li>
		</ul>	
</template>

<script lang="ts">
import { utilsMixin } from "../mixins/utilsmixin";
export default {
  props:{
    games:Array,
    title:String,
  },
  mixins:[utilsMixin],
  data() {
    return {
    
    }
  }
}
</script>