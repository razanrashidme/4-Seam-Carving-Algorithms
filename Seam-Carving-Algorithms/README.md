# Content-Aware Image Resizing (Seam Carving)

## Project Overview
This project implements the **Seam Carving** algorithm for content-aware image resizing using **Java**. Unlike standard scaling or cropping, this method iteratively removes the least important pixels (vertical seams) based on an energy map, preserving key objects and features while reducing image width.



## Key Features
* **Three Algorithms Implemented:**
    * **Brute Force:** Recursively explores all paths (Exponential Time (3^H)).
    * **Greedy:** Choose locally optimal pixels (Fast but inaccurate).
    * **Dynamic Programming (Optimized):** Finds the global minimum energy seam efficiently (Linear Time O(W×H)).
* **Performance Optimization:** Successfully optimized the solution from exponential to linear time complexity.
* **Vertical Resizing:** Focuses on reducing image width by identifying and removing vertical paths of low energy.

## Complexity Analysis
| Algorithm | Time Complexity | Space Complexity | Notes |
|-----------|----------------|------------------|-------|
| **Brute Force** | $O(3^H)$ | $O(H)$ | Too slow for large images |
| **Greedy** | $O(W \times H)$ | $O(W \times H)$ | Distortion risk |
| **Dynamic Programming** | $O(W \times H)$ | $O(W \times H)$ | **Optimal Solution** |

## Tools and Technologies
* **Language:** Java
* **Concepts:** Dynamic Programming, Recursion, Pixel Energy Calculation.

## Contributors
This project was a collaborative team effort by:
- Saadiya Abdulqader 
- Reema Almadhi 
- Rzan Rashid

