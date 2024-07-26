from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
from flask_jwt_extended import JWTManager, create_access_token, jwt_required

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///meritmatch.db'
app.config['JWT_SECRET_KEY'] = 'your_jwt_secret_key'

db = SQLAlchemy(app)
ma = Marshmallow(app)
jwt = JWTManager(app)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), unique=True, nullable=False)
    password = db.Column(db.String(100), nullable=False)
    karma_points = db.Column(db.Integer, default=0)

class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200), nullable=False)
    description = db.Column(db.String(500), nullable=False)
    karma_points = db.Column(db.Integer, nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    reserved = db.Column(db.Boolean, default=False)
    completed = db.Column(db.Boolean, default=False)

class UserSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = User

class TaskSchema(ma.SQLAlchemyAutoSchema):
    class Meta:
        model = Task

user_schema = UserSchema()
users_schema = UserSchema(many=True)
task_schema = TaskSchema()
tasks_schema = TaskSchema(many=True)

@app.route('/register', methods=['POST'])
def register():
    username = request.json['username']
    password = request.json['password']
    new_user = User(username=username, password=password)
    db.session.add(new_user)
    db.session.commit()
    return user_schema.jsonify(new_user)

@app.route('/login', methods=['POST'])
def login():
    username = request.json['username']
    password = request.json['password']
    user = User.query.filter_by(username=username, password=password).first()
    if user:
        access_token = create_access_token(identity=user.id)
        return jsonify(access_token=access_token)
    return jsonify({"msg": "Bad username or password"}), 401

@app.route('/tasks', methods=['GET'])
@jwt_required()
def get_tasks():
    tasks = Task.query.all()
    return tasks_schema.jsonify(tasks)

@app.route('/tasks', methods=['POST'])
@jwt_required()
def add_task():
    title = request.json['title']
    description = request.json['description']
    karma_points = request.json['karma_points']
    user_id = request.json['user_id']
    new_task = Task(title=title, description=description, karma_points=karma_points, user_id=user_id)
    db.session.add(new_task)
    db.session.commit()
    return task_schema.jsonify(new_task)

if __name__ == '__main__':
    db.create_all()
    app.run(debug=True)
