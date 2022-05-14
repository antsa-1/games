<template>
	<div class="row">
		<div class="col">
            <!--
			<button v-if="resignButtonVisible" :disabled ="resignButtonDisabled" @click="resign" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Resign
			</button>			
			<button v-if="rematchButtonEnabled" @click="rematch" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Rematch
			</button>
            -->
		</div>
	</div>

	<div class="row">
		<div class="col-xs-12 col-sm-4">
			YatzyTable
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeMount, onUpdated, watch } from 'vue'
    import { useRouter} from 'vue-router'
  

    // @ts-ignore
 
    const router = useRouter()
    const abc = ref("")

    
    
    const doSmth = (() => {
        console.log("smth")
    })

    onBeforeMount(() => {    
        console.log("onBeforeMount")
    })
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.hidden{
	visibility:hidden
}
</style>
