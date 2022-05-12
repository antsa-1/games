/**
 * @author antsa-1 from GitHub
 */
import { createApp } from 'vue'
import Games from './Games.vue'
import Login from './components/Login.vue'
import Lobby from './components/Lobby.vue'
import Home from './components/Home.vue'
import TableTicTacToe from './components/tables/TableTicTacToe.vue'
import { IGameToken, IPlayer, ITable } from "./interfaces/interfaces";

import Profile from './components/Profile.vue'
import TopLists from './components/highscores/TopLists.vue'
import TableConnectFour from './components/tables/TableConnectFour.vue'
import PoolTable from './components/tables/PoolTable.vue'
import Info from './components/Info.vue'
import Instructions from './components/Instructions.vue'
import Feedback from './components/Feedback.vue'
import Registration from './components/Registration.vue'
import Error from './components/Error.vue'
import VueRouter, { RouteRecordRaw } from 'vue-router'
import { store } from './store'
import { IUser } from "./interfaces/interfaces";
import { createWebHistory, createRouter } from "vue-router";

const routes: RouteRecordRaw[] = [
    {
        path: '/portal/index.html',
        redirect: { name: 'Home' }
    },
    {
        path: '/index.html',
        redirect: { name: 'Home' }
    },
    {
        path: '/',
        redirect: { name: 'Home' }
    },
    {
        path: '',
        redirect: { name: 'Home' }
    },
    {
        path: '/portal',
        redirect: { name: 'Home' }
    },
    {
        path: '/portal/home',
        component: Home,
        name: "Home",
    },
    {
        path: '/portal/instructions',
        component: Instructions,
        name: "Instructions",
    },

    {
        path: '/portal/login',
        component: Login,
        name: "Login",
        props: true,
    },
    {
        path: '/portal/registration',
        component: Registration,
        name: "Registration",
        props: true,
    },
    {
        path: '/portal/toplists',
        component: TopLists,
        name: "TopLists",
        props: true,
    },
    {
        path: '/portal/lobby',
        component: Lobby,
        name: "Lobby",
        props: true,
        beforeEnter: (to, from, next) => {
            if (!store.state.user) {
                const userName = sessionStorage.getItem("userName")
                const token = sessionStorage.getItem("token")
                let user: IUser = { name: userName, token: token, webSocket: null }
                store.dispatch("setUser", user).then(() => {
                    next()
                })
            } else {
                next()
            }
        }
    },
    {
        path: '/portal/table/:watch?',
        component: TableTicTacToe,
        name: "TableTicTacToe",
        props: true,
        beforeEnter: (to, from, next) => {
            if (!store.state.user || !store.state.theTable) {
                next('/');
            } else {
                next()
            }
        }
    },
    {
        path: '/portal/connectfour/:watch?',
        component: TableConnectFour,
        name: "TableConnectFour",        
        beforeEnter: (to, from, next) => {
            if (!store.state.user || !store.state.theTable) {
                next('/');
            } else {
                next()
            }
        }
    },
    {
        path: '/portal/pool/:watch?',
        component: PoolTable,
        name: "PoolTable",
        props: true,  
        beforeEnter: (to, from, next) => {
            if (!store.state.user || !store.state.theTable) {
                next('/');
            } else {
                next()
            }
        }
    },
    {
        path: '/portal/user/:selectedName/profile',
        component: Profile,
        name: "Profile",
        props: true,      
    },
    {
        path: '/portal/terms',
        component: Info,
        name: "Info",
    },
    {
        path: '/portal/feedback',
        component: Feedback,
        name: "Feedback",
    },
    {
        path: "/portal/:pathMatch(.*)*",
        component: Home,
        name: "Home",
    },
    {
        path: "/portal/:catchAll(.*)",
        component: Error,
        name: "Error",
        props: true,
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
});
const app = createApp(Games)

app.use(router).use(store).mount("#app");