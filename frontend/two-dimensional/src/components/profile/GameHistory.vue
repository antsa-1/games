<template>
    <span class="fw-bold ">
        {{ title }}
    </span>
    <ul class="list-group ">
        <li v-for="(game, index) in games" :key="game.gameId" class="list-group-item"
            :class="[index % 2 == 0 ? 'bg-success p-2 text-dark bg-opacity-25' : 'bg-success p-2 text-dark bg-opacity-10']">
            <div class="">
                {{ convertInstantToDate(game.startInstant) }}
                <br>
                <p v-if="game.players.length == 2">
                    {{ game.players[0].name }}
                    <template v-if="game.players[0].initialRanking >= 0">
                        {{ fixVal(game.players[0].initialRanking) }} {{ getRankingChange(game.players[0]) }}
                    </template>
                    -
                    {{ game.players[1].name }}
                    <template v-if="game.players[1].initialRanking >= 0">
                        {{ fixVal(game.players[1].initialRanking) }} {{ getRankingChange(game.players[1]) }}
                    </template>
                    <br>
                    <template v-if="game.players[0].score >= 0 || game.players[1].score >= 0">
                        Score: {{ game.players[0].score }} - {{ game.players[1].score }}
                        <br>
                    </template>

                    <template v-if="game.winner">
                        Winner: {{ game.winner }}
                        <br>
                    </template>
                    <template v-if="game.result">
                        Result: {{ convertGameResultToText(game.result) }}
                        <br>
                    </template>
                    <template v-if="game.finishStatus">
                        Game status: {{ toLowerCase(game.finishStatus) }}
                        <br>
                    </template>
                    <template v-if="game.timeControlSeconds">
                        TimeControl: {{ game.timeControlSeconds }} s.
                        <br>
                    </template>
                    <template v-if="game.description && game.gameType < 30">
                        Description: {{ game.description }}
                    </template>
                </p>
                <ul v-if="game.players.length > 2" class="multiplayer-list text-start">
                    <li v-for="(player, index) in game.players" :key="player.name">
                        <span class="text-bold">{{ player.name }}</span>
                        <br>
                        Ranking: {{ fixVal(game.players[index].initialRanking) }}
                        {{ getRankingChange(game.players[index]) }}
                        <br>
                        <template v-if="player.score >= 0">
                            Score: {{ player.score }}
                            <br>
                        </template>
                        <template v-if="player.position">
                            Position: {{ player.position }}
                        </template>
                    </li>
                    <template v-if="game.finishStatus">
                        Game status: {{ toLowerCase(game.finishStatus) }}
                        <br>
                    </template>
                    <template v-if="game.timeControlSeconds">
                        TimeControl: {{ game.timeControlSeconds }} s.
                        <br>
                    </template>
                </ul>
            </div>
        </li>
    </ul>
</template>
<script setup lang="ts">
import { convertInstantToDate, fixVal, getRankingChange } from "../../components/composables/utilsComposable"
import { IGame, GameResult, GameStatus } from "../../interfaces/interfaces"
import { defineComponent } from "vue";


const props = defineProps({
    title: String,
    games: Array<IGame>
})

const convertGameResultToText = (result: number) => {
    const val = GameResult[result]
    return val.toLowerCase()
}

const toLowerCase = (status: string) => {
    return status.toLowerCase()
}

</script>
<style scoped>
.multiplayer-list span {
    font-weight: bold
}
</style>

<!--
 * 	@author antsa-1 from GitHub 13.07.2022
 -->