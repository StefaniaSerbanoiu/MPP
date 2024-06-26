import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
//import shoesData from './db.json'; // no longer used
import { saveAs } from 'file-saver';
import { toast } from 'react-toastify'; 
import useStore from './useStore';
import useTokenStore from './Stores/useTokenStore';
import useUserStore from './Stores/useUserStore';

function Home() {
  //const [data, setData] = useState([]);
  const [selectedItems, setSelectedItems] = useState([]);
  const { data, setData, token, username } = useStore();
 // const token = useStore.getState().token; // Get token from store
 // const username = useStore.getState().username; // Get username from store
    const tokenFromStore = useTokenStore((state) => state.token)
    const setToken = useTokenStore((state) => state.setToken)

    const usernameFromStore = useUserStore((state) => state.username)
    const setUsernameFromStore = useUserStore((state) => state.setUsername)


    useEffect(() => {
      console.log("Token:", tokenFromStore);
      //console.log("Username from store:", usernameFromStore);
    }, [tokenFromStore, usernameFromStore]);
 useEffect(() => {
  //console.log("username from store: ", useUserStore.getState().username)

    axios.get(`https://charming-cooperation-production.up.railway.app/shoe/get/${usernameFromStore}`, {
    //axios.get(`http://localhost:8080/shoe/get/${usernameFromStore}`, {
    headers: {
      Authorization: `Bearer ${tokenFromStore}` // Include token in the request headers
    }
  })
  .then(response => {
    // Extract data from the response
    console.log(response)
    setData(response.data);
    // Save data to local storage
    localStorage.setItem('shoeData', JSON.stringify(response.data));
    
    const lastItem = response.data.slice(-1)[0];
    //console.log('Last item from backend:', lastItem);

    const lastItem2 = JSON.parse(localStorage.getItem('shoeData')).pop();
    //console.log('Last item from local storage:', lastItem2);
  })
  .catch(error => {
    console.error('Error fetching data:', error);
    // Retrieve data from local storage if backend call fails
    const storedData = JSON.parse(localStorage.getItem('shoeData'));
    
    if (storedData) {
      setData(storedData);
    } else {
      toast.error('Failed to fetch shoe data');
    }
  });
}, [setData, username, token]);






    const handleDelete = (id) => {
      const confirmDeleteMessage = window.confirm("Are you sure you want to delete this product? Deletion of products can't be undone.");
      if (confirmDeleteMessage) {
         axios.delete(`https://charming-cooperation-production.up.railway.app/shoe/delete/${id}`, {
         // axios.delete(`http://localhost:8080/shoe/delete/${id}`, {
          headers: {
            Authorization: `Bearer ${tokenFromStore}` // Include token in the request headers
          }
        })
          .then(result => {
            location.reload();
            window.confirm("Product deleted");
          })

          .catch(err => {
            console.error(err);
            // Delete the item with the given id from storedData
            let storedData = JSON.parse(localStorage.getItem('shoeData')) || [];
            storedData = storedData.filter(item => item.shoe_id !== id);
            localStorage.setItem('shoeData', JSON.stringify(storedData));
            window.confirm("Product deleted (backend is offline, so changes won't remain)");
            location.reload();
          });
      }
    }
  

  const handleCheckboxChange = (id) => {
    setSelectedItems(prevSelectedItems => {
      if (prevSelectedItems.includes(id)) {
        return prevSelectedItems.filter(itemId => itemId !== id);
      } else {
        return [...prevSelectedItems, id];
      }
    });
  };

  const handleBulkDelete = () => {
    const confirmDeleteMessage = window.confirm("Are you sure you want to delete the selected products? Deletion of products can't be undone.");
    if (confirmDeleteMessage) {
      // Delete all selected items
      selectedItems.forEach(shoe_id => {
        axios.delete(`https://charming-cooperation-production.up.railway.app/shoe/delete/${shoe_id}`, {
        //axios.delete(`http://localhost:8080/shoe/delete/${shoe_id}`, {
          headers: {
            Authorization: `Bearer ${tokenFromStore}` // Include token in the request headers
          }
        })
          .then(result => {
             
          })
          .catch(err => console.log(err));
      });
      setSelectedItems([]); // Clear selected items after deletion
      setData(prevData => prevData.filter(item => item.shoe_id !== id));
            window.confirm("Products deleted");
            location.reload();  
    }
  };

  function log_out() {
     //axios.post('http://localhost:8080/logout')
    axios.post('https://charming-cooperation-production.up.railway.app/logout', 
        {
          headers: {
            Authorization: `Bearer ${tokenFromStore}` // Include token in the request headers
          }
        })
     
        .then(result => {
          console.log(result);
          navigate(`/login`);
        })
        .catch(err => console.log("Error while logging out : ", err));
  }

  const handleExportCSV = () => {
    const selectedData = data.filter(item => selectedItems.includes(item.shoe_id));
    const csvContent = convertToCSV(selectedData);
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' });
    saveAs(blob, 'selected_data.csv');
  };

  const handleExportJSON = () => {
    const selectedData = data.filter(item => selectedItems.includes(item.shoe_id));
    const jsonContent = JSON.stringify(selectedData, null, 2);
    const blob = new Blob([jsonContent], { type: 'application/json' });
    saveAs(blob, 'selected_data.json');
  };

  const convertToCSV = (data) => {
    const header = Object.keys(data[0]).join(',');
    const rows = data.map(obj => Object.values(obj).join(','));
    return header + '\n' + rows.join('\n');
  };

  function sortASCprice() {
    //const url = `http://localhost:8080/shoe/get/${usernameFromStore}?sortBy=price&sortOrder=asc`;
    const url = `https://charming-cooperation-production.up.railway.app/shoe/get/${usernameFromStore}?sortBy=price&sortOrder=asc`;
    
    fetch(url, {
      headers: {
        Authorization: `Bearer ${tokenFromStore}` // Include token in the request headers
      }
    })
    .then(response => response.json())
    .then(shoes => { 
      updateShoeList(shoes); // Update the UI with the sorted shoe list
    })
    .catch(error => console.error('Error sorting shoes ascendingly:', error));
  }
  

  function sortDESCprice() {
    //const url = `http://localhost:8080/shoe/get/${usernameFromStore}?sortBy=price&sortOrder=desc`;
    const url = `https://charming-cooperation-production.up.railway.app/shoe/get/${usernameFromStore}?sortBy=price&sortOrder=desc`;
    
    fetch(url, {
      headers: {
        Authorization: `Bearer ${tokenFromStore}` // Include token in the request headers
      }
    })
    .then(response => response.json())
    .then(shoes => { 
      updateShoeList(shoes); // Update the UI with the sorted shoe list
    })
    .catch(error => console.error('Error sorting shoes descendingly:', error));
  }
  

function updateShoeList(shoes) {
  setData(shoes); // Update the shoe list in the state
  localStorage.setItem('shoeData', JSON.stringify(shoes)); // Also, updating the local storage with the new data
}


  return (
    <div className='d-flex flex-column justify-content-center align-items-center center'>
      <h1>List of products of {usernameFromStore}</h1>
      <div className='w=75 rounded border shadow p-4'>
        <div className='d-flex justify-content-end mb-3'>
          <button onClick={handleBulkDelete} className='btn btn-danger me-2'>Delete Selected</button>
          <button onClick={handleExportCSV} className='btn btn-primary me-2'>Export CSV</button>
          <button onClick={handleExportJSON} className='btn btn-success me-2'>Export JSON</button>
          <button onClick={sortASCprice} className='btn btn-warning me-2'>ASC price</button>
          <button onClick={sortDESCprice} className='btn btn-warning me-2'>DESC price</button>
          <Link to="/create" className='btn btn-light'>New +</Link>
        </div>
        <table className='table table-stipend'>
          <thead>
            <tr>
              <th>Select</th>
              <th>Product Name</th>
              <th>Size</th>
              <th>Price</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {data.map((d, index) => (
              <tr key={index}>
                <td>
                  <input
                    type="checkbox"
                    checked={selectedItems.includes(d.shoe_id)}
                    onChange={() => handleCheckboxChange(d.shoe_id)}
                  />
                </td>
                <td>{d.product_name}</td>
                <td>{d.size}</td>
                <td>{d.price}</td>
                <td>
                  <Link to={`/update/${d.shoe_id}`} className='btn btn-sm btn-primary me-2'>Edit</Link>
                  <button onClick={() => handleDelete(d.shoe_id)} className='btn btn-sm btn-danger me-2'>Delete</button>
                  <Link to={`/suggestions/${d.shoe_id}`} className='btn btn-sm btn-info'>See style advice</Link>
                  <Link to={`/read/${d.shoe_id}`} className='btn btn-sm btn-info'>More</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <Link to={'/login'} className='btn btn-light me-2'>Log out</Link>
      </div>
    </div>
  );
}




export default Home;