import { combineReducers } from 'redux';
import appPage from './components/reducer';
import geocoder from './geocoder/reducer';

const mainReducer = combineReducers({
    appPage,
    geocoder,
});

export default mainReducer;
