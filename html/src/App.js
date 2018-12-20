import React, { Component } from 'react';
import axios from 'axios';

import logo from './logo.svg';
import './App.css';
import Small from './products/small/Small';



class ListOfProducts extends Component {

  constructor(props) {
    super(props);
    this.state = {
      products: []
    }
  }

  componentDidMount() {
    axios.get(`/products.json`)
      .then(res => {
        const products = res.data;
        this.setState({ products });
      })
  }

  render() {
    return (
      <div>
        {this.state.products.map(product => <div><Small {...product}/></div>)}
      </div>

    )
  }
}

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <p>
            Store
          </p>
        </header>
        <ListOfProducts/>
      </div>
    );
  }
}

export default App;
