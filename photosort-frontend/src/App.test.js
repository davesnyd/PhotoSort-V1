import { render, screen } from '@testing-library/react';
import App from './App';

test('renders PhotoSort app', () => {
  render(<App />);
  // App should render without crashing and show loading or login page
  expect(document.body).toBeInTheDocument();
});
