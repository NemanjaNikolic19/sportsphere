import { Routes } from '@angular/router';
import { Home } from './pages/public/home/home';
import { Login } from './pages/public/login/login';
import { AdminDashboard } from './pages/admin/admin-dashboard/admin-dashboard';
import { AthleteDashboard } from './pages/athlete/athlete-dashboard/athlete-dashboard';
import { EmployeeDashboard } from './pages/employee/employee-dashboard/employee-dashboard';
import { PublicSearch } from './pages/public/search/search';
import { FacilityDetails } from './pages/public/facility-details/facility-details';
import { Register } from './pages/public/register/register';
import { AdminUsers } from './pages/admin/admin-users/admin-users';
import { AdminPendingUsers } from './pages/admin/admin-pending-users/admin-pending-users';
import { AdminPendingFacilities } from './pages/admin/admin-pending-facilities/admin-pending-facilities';
import { AthleteProfile } from './pages/athlete/athlete-profile/athlete-profile';
import { AthleteReservations } from './pages/athlete/athlete-reservations/athlete-reservations';
import { AthleteTeammates } from './pages/athlete/athlete-teammates/athlete-teammates';
import { AthleteTraining } from './pages/athlete/athlete-training/athlete-training';
import { AthleteShop } from './pages/athlete/athlete-shop/athlete-shop';
import { EmployeeProfile } from './pages/employee/employee-profile/employee-profile';
import { EmployeeFacilities } from './pages/employee/employee-facilities/employee-facilities';
import { EmployeeReservations } from './pages/employee/employee-reservations/employee-reservations';
import { EmployeeEquipment } from './pages/employee/employee-equipment/employee-equipment';
import { EmployeePromotions } from './pages/employee/employee-promotions/employee-promotions';
import { Promotions } from './pages/public/promotions/promotions';
import { EmployeeReports } from './pages/employee/employee-reports/employee-reports';
import { AdminCatalog } from './pages/admin/admin-catalog/admin-catalog';
import { ForgotPassword } from './pages/public/forgot-password/forgot-password';
import { ResetPassword } from './pages/public/reset-password/reset-password';
import { AthleteStatistics } from './pages/athlete/athlete-statistics/athlete-statistics';
import { ChangeProfilePicture } from './pages/shared/change-profile-picture/change-profile-picture';

export const routes: Routes = [
    {
        path: '',
        component: Home
    },
    {
        path: 'search',
        component: PublicSearch
    },
    {
        path: 'facilities/:id',
        component: FacilityDetails
    },
    {
        path: 'promotions',
        component: Promotions
    },
    {
        path: 'login',
        component: Login
    },
    {
        path: 'register',
        component: Register
    },
    {
        path: 'admin-login',
        component: Login
    },
    {
        path: 'forgot-password',
        component: ForgotPassword
    },
    {
        path: 'reset-password',
        component: ResetPassword
    },
    {
        path: 'admin',
        component: AdminDashboard
    },
    {
        path: 'admin/users',
        component: AdminUsers
    },
    {
        path: 'admin/users/pending',
        component: AdminPendingUsers
    },
    {
        path: 'admin/facilities/pending',
        component: AdminPendingFacilities
    },
    {
        path: 'admin/catalog',
        component: AdminCatalog
    },
    {
        path: 'athlete',
        component: AthleteDashboard
    },
    {
        path: 'athlete/profile/:id',
        component: AthleteProfile
    },
    {
        path: 'athlete/profile',
        component: AthleteProfile
    },
    {
        path: 'athlete/teammates',
        component: AthleteTeammates
    },
    {
        path: 'athlete/reservations',
        component: AthleteReservations
    },
    {
        path: 'athlete/training',
        component: AthleteTraining
    },
    {
        path: 'athlete/statistics',
        component: AthleteStatistics
    },
    {
        path: 'athlete/shop',
        component: AthleteShop
    },
    {
        path: 'employee',
        component: EmployeeDashboard
    },
    {
        path: 'employee/profile',
        component: EmployeeProfile
    },
    {
        path: 'employee/facilities',
        component: EmployeeFacilities
    },
    {
        path: 'employee/reservations',
        component: EmployeeReservations
    },
    {
        path: 'employee/equipment',
        component: EmployeeEquipment
    },
    {
        path: 'employee/promotions',
        component: EmployeePromotions
    },
    {
        path: 'employee/reports',
        component: EmployeeReports
    },
    {
        path: 'profile/change-picture',
        component: ChangeProfilePicture
    },
    {
        path: '**',
        redirectTo: ''
    }
];
