package com.example.helloworld.core;

public class UpdatePositionBean {
	Category categoryToUpdate;
	float siblingNumberPreviousNode;
	float siblingNumbeNextNode;

	public UpdatePositionBean() {

	}

	public Category getCategoryToUpdate() {
		return categoryToUpdate;
	}

	public void setCategoryToUpdate(Category categoryToUpdte) {
		this.categoryToUpdate = categoryToUpdte;
	}

	public float getSiblingNumberPreviousNode() {
		return siblingNumberPreviousNode;
	}

	public void setSiblingNumberPreviousNode(float siblingNumberPreviousNode) {
		this.siblingNumberPreviousNode = siblingNumberPreviousNode;
	}

	public float getSiblingNumbeNextNode() {
		return siblingNumbeNextNode;
	}

	public void setSiblingNumbeNextNode(float siblingNumbeNextNode) {
		this.siblingNumbeNextNode = siblingNumbeNextNode;
	}

}
