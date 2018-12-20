import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import Small from './products/small/Small';


const DummyProduct = {
  name: 'Jamon de Bellota',
  description: 'Jamón de bellota pata negra procedente de cerdos ibéricos puros 100 %, criados en libertad y alimentados con bellotas en montanera en las dehesas de la Sierra de Huelva, actual Parque Natural Sierra de Aracena y Picos de Aroche, donde se encuentra Jabugo. Elaboración tradicional siguiendo el proceso artesanal de los maestros jamoneros y lenta curación en secadero natural. (Precinto negro)',
  price: 34.55
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
        <Small {...DummyProduct}/>
      </div>
    );
  }
}

export default App;
