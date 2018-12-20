import React, { Component } from 'react';

import {List} from './products/list/List'

import logo from './logo.svg';
import './App.css';




class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <p>
            Store
          </p>
        </header>
        <List/>
      </div>
    );
  }
}

export default App;
