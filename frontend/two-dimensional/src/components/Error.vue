<template>
    <div class="row">
        <div class="col text-danger">
            Timeout or an error occured.
        </div>
    </div>
</template>  

<script setup lang="ts">
import { onMounted } from 'vue';
import { useStore } from 'vuex';

const store = useStore()

onMounted(() => {
   // store.dispatch("logout")
})

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
